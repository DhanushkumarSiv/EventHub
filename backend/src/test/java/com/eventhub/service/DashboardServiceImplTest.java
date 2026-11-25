package com.eventhub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eventhub.dto.response.DashboardStatsResponse;
import com.eventhub.entity.AttendanceStatus;
import com.eventhub.entity.Event;
import com.eventhub.entity.EventStatus;
import com.eventhub.entity.RegistrationStatus;
import com.eventhub.repository.AttendanceRepository;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.service.impl.DashboardServiceImpl;

class DashboardServiceImplTest {

    private EventRepository eventRepository;
    private RegistrationRepository registrationRepository;
    private AttendanceRepository attendanceRepository;
    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        Event event1 = Event.builder()
                .id(1L)
                .title("Workshop")
                .eventDate(Instant.now().plus(5, ChronoUnit.DAYS))
                .maxParticipants(50)
                .status(EventStatus.PUBLISHED)
                .build();

        eventRepository = (EventRepository) Proxy.newProxyInstance(
                EventRepository.class.getClassLoader(),
                new Class<?>[]{EventRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("count".equals(name)) return 10L;
                    if ("countByStatus".equals(name)) {
                        EventStatus status = (EventStatus) args[0];
                        if (status == EventStatus.PUBLISHED) return 6L;
                        if (status == EventStatus.DRAFT) return 2L;
                        if (status == EventStatus.COMPLETED) return 1L;
                        if (status == EventStatus.CANCELLED) return 1L;
                        return 0L;
                    }
                    if ("findTop5ByStatusAndEventDateAfterOrderByEventDateAsc".equals(name)) {
                        return List.of(event1);
                    }
                    if ("findAll".equals(name)) {
                        return List.of(event1);
                    }
                    return null;
                }
        );

        registrationRepository = (RegistrationRepository) Proxy.newProxyInstance(
                RegistrationRepository.class.getClassLoader(),
                new Class<?>[]{RegistrationRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("countByStatus".equals(name)) {
                        return 45L; // total active registrations
                    }
                    if ("countByEventIdAndStatus".equals(name)) {
                        return 25L; // registrations for event1
                    }
                    return null;
                }
        );

        attendanceRepository = (AttendanceRepository) Proxy.newProxyInstance(
                AttendanceRepository.class.getClassLoader(),
                new Class<?>[]{AttendanceRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("countByStatus".equals(name)) {
                        return 20L; // total attendance
                    }
                    if ("countByEventIdAndStatus".equals(name)) {
                        return 15L; // attendance for event1
                    }
                    return null;
                }
        );

        dashboardService = new DashboardServiceImpl(eventRepository, registrationRepository, attendanceRepository);
    }

    @Test
    void getDashboardStatsAggregatesMetricsCorrectly() {
        DashboardStatsResponse stats = dashboardService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(10L, stats.getTotalEvents());
        assertEquals(6L, stats.getPublishedEvents());
        assertEquals(2L, stats.getDraftEvents());
        assertEquals(1L, stats.getCompletedEvents());
        assertEquals(1L, stats.getCancelledEvents());
        assertEquals(45L, stats.getTotalRegistrations());
        assertEquals(20L, stats.getTotalAttendance());
        assertEquals(1, stats.getUpcomingEvents().size());
        assertEquals("Workshop", stats.getUpcomingEvents().get(0).getTitle());
        assertEquals(1, stats.getRegistrationsByEvent().size());
        assertEquals(25L, stats.getRegistrationsByEvent().get(0).getRegisteredCount());
        assertEquals(15L, stats.getRegistrationsByEvent().get(0).getAttendanceCount());
    }
}
