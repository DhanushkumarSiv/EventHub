package com.eventhub.service.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventhub.dto.response.DashboardStatsResponse;
import com.eventhub.dto.response.EventRegistrationStat;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.entity.AttendanceStatus;
import com.eventhub.entity.Event;
import com.eventhub.entity.EventStatus;
import com.eventhub.entity.RegistrationStatus;
import com.eventhub.mapper.EventMapper;
import com.eventhub.repository.AttendanceRepository;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.service.DashboardService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final AttendanceRepository attendanceRepository;

    public DashboardServiceImpl(
            EventRepository eventRepository,
            RegistrationRepository registrationRepository,
            AttendanceRepository attendanceRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long totalEvents = eventRepository.count();
        long publishedEvents = eventRepository.countByStatus(EventStatus.PUBLISHED);
        long draftEvents = eventRepository.countByStatus(EventStatus.DRAFT);
        long completedEvents = eventRepository.countByStatus(EventStatus.COMPLETED);
        long cancelledEvents = eventRepository.countByStatus(EventStatus.CANCELLED);

        long totalRegistrations = registrationRepository.countByStatus(RegistrationStatus.REGISTERED);
        long totalAttendance = attendanceRepository.countByStatus(AttendanceStatus.PRESENT);

        Instant now = Instant.now();
        List<Event> upcomingEventEntities = eventRepository
                .findTop5ByStatusAndEventDateAfterOrderByEventDateAsc(EventStatus.PUBLISHED, now);

        List<EventResponse> upcomingEvents = upcomingEventEntities.stream()
                .map(event -> {
                    long count = registrationRepository.countByEventIdAndStatus(event.getId(), RegistrationStatus.REGISTERED);
                    return EventMapper.toResponse(event, count);
                })
                .toList();

        List<Event> allEvents = eventRepository.findAll();
        List<EventRegistrationStat> registrationsByEvent = allEvents.stream()
                .map(event -> {
                    long registeredCount = registrationRepository.countByEventIdAndStatus(
                            event.getId(), RegistrationStatus.REGISTERED);
                    long attendanceCount = attendanceRepository.countByEventIdAndStatus(
                            event.getId(), AttendanceStatus.PRESENT);

                    return EventRegistrationStat.builder()
                            .eventId(event.getId())
                            .eventTitle(event.getTitle())
                            .maxParticipants(event.getMaxParticipants() != null ? event.getMaxParticipants() : 0)
                            .registeredCount(registeredCount)
                            .attendanceCount(attendanceCount)
                            .build();
                })
                .toList();

        return DashboardStatsResponse.builder()
                .totalEvents(totalEvents)
                .publishedEvents(publishedEvents)
                .draftEvents(draftEvents)
                .completedEvents(completedEvents)
                .cancelledEvents(cancelledEvents)
                .totalRegistrations(totalRegistrations)
                .totalAttendance(totalAttendance)
                .upcomingEvents(upcomingEvents)
                .registrationsByEvent(registrationsByEvent)
                .build();
    }
}
