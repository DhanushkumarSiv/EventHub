package com.eventhub.service.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventhub.dto.request.EventRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.EventStatus;
import com.eventhub.entity.RegistrationStatus;
import com.eventhub.exception.BadRequestException;
import com.eventhub.exception.ConflictException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.mapper.EventMapper;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.service.EventService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    public EventServiceImpl(
            EventRepository eventRepository,
            RegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    @Transactional
    public EventResponse createEvent(EventRequest request) {
        validateEventDatesAndCapacity(request);

        Event event = Event.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .venue(request.getVenue().trim())
                .eventDate(request.getEventDate())
                .registrationDeadline(request.getRegistrationDeadline())
                .maxParticipants(request.getMaxParticipants())
                .status(EventStatus.DRAFT)
                .build();

        Event savedEvent = eventRepository.save(event);
        log.info("Created new draft event with id: {} and title: '{}'", savedEvent.getId(), savedEvent.getTitle());

        return EventMapper.toResponse(savedEvent, 0);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new ConflictException("Cannot update a cancelled event");
        }
        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new ConflictException("Cannot update a completed event");
        }

        validateEventDatesAndCapacity(request);

        long activeRegistrations = registrationRepository.countByEventIdAndStatus(id, RegistrationStatus.REGISTERED);
        if (request.getMaxParticipants() < activeRegistrations) {
            throw new ConflictException(String.format(
                    "Maximum participants (%d) cannot be lower than existing registrations (%d)",
                    request.getMaxParticipants(), activeRegistrations));
        }

        event.setTitle(request.getTitle().trim());
        event.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        event.setVenue(request.getVenue().trim());
        event.setEventDate(request.getEventDate());
        event.setRegistrationDeadline(request.getRegistrationDeadline());
        event.setMaxParticipants(request.getMaxParticipants());

        Event updatedEvent = eventRepository.save(event);
        log.info("Updated event id: {}", updatedEvent.getId());

        return EventMapper.toResponse(updatedEvent, activeRegistrations);
    }

    @Override
    @Transactional
    public EventResponse publishEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new ConflictException("Cannot publish a cancelled event");
        }
        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new ConflictException("Cannot publish a completed event");
        }
        if (event.getRegistrationDeadline().isBefore(Instant.now())) {
            throw new BadRequestException("Cannot publish an event whose registration deadline has already passed");
        }

        event.setStatus(EventStatus.PUBLISHED);
        Event saved = eventRepository.save(event);
        log.info("Published event id: {}", saved.getId());

        long count = registrationRepository.countByEventIdAndStatus(id, RegistrationStatus.REGISTERED);
        return EventMapper.toResponse(saved, count);
    }

    @Override
    @Transactional
    public EventResponse cancelEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new ConflictException("Cannot cancel an event that is already completed");
        }

        event.setStatus(EventStatus.CANCELLED);
        Event saved = eventRepository.save(event);
        log.info("Cancelled event id: {}", saved.getId());

        long count = registrationRepository.countByEventIdAndStatus(id, RegistrationStatus.REGISTERED);
        return EventMapper.toResponse(saved, count);
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new ConflictException("Only DRAFT events can be deleted. Cancel the event instead.");
        }

        eventRepository.delete(event);
        log.info("Deleted draft event id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEventsForAdmin() {
        return eventRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(event -> {
                    long count = registrationRepository.countByEventIdAndStatus(event.getId(), RegistrationStatus.REGISTERED);
                    return EventMapper.toResponse(event, count);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getPublishedEvents() {
        return eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED).stream()
                .map(event -> {
                    long count = registrationRepository.countByEventIdAndStatus(event.getId(), RegistrationStatus.REGISTERED);
                    return EventMapper.toResponse(event, count);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        long count = registrationRepository.countByEventIdAndStatus(id, RegistrationStatus.REGISTERED);
        return EventMapper.toResponse(event, count);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getPublishedEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Published event", "id", id);
        }

        long count = registrationRepository.countByEventIdAndStatus(id, RegistrationStatus.REGISTERED);
        return EventMapper.toResponse(event, count);
    }

    private void validateEventDatesAndCapacity(EventRequest request) {
        if (request.getMaxParticipants() == null || request.getMaxParticipants() <= 0) {
            throw new BadRequestException("Maximum participants must be greater than zero");
        }

        if (request.getRegistrationDeadline() == null || request.getEventDate() == null) {
            throw new BadRequestException("Event date and registration deadline are required");
        }

        if (!request.getRegistrationDeadline().isBefore(request.getEventDate())) {
            throw new BadRequestException("Registration deadline must be strictly before the event date");
        }
    }
}
