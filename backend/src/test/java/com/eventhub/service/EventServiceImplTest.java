package com.eventhub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eventhub.dto.request.EventRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.EventStatus;
import com.eventhub.exception.BadRequestException;
import com.eventhub.exception.ConflictException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.service.impl.EventServiceImpl;

class EventServiceImplTest {

    private EventRepository eventRepository;
    private RegistrationRepository registrationRepository;
    private EventServiceImpl eventService;

    private Event storedEvent;
    private long registrationCount = 0;

    @BeforeEach
    void setUp() {
        storedEvent = Event.builder()
                .id(1L)
                .title("Tech Summit 2026")
                .description("Annual summit")
                .venue("Auditorium")
                .eventDate(Instant.now().plus(30, ChronoUnit.DAYS))
                .registrationDeadline(Instant.now().plus(20, ChronoUnit.DAYS))
                .maxParticipants(100)
                .status(EventStatus.DRAFT)
                .build();

        registrationCount = 10;

        eventRepository = (EventRepository) Proxy.newProxyInstance(
                EventRepository.class.getClassLoader(),
                new Class<?>[]{EventRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        Event e = (Event) args[0];
                        if (e.getId() == null) {
                            e.setId(10L);
                        }
                        storedEvent = e;
                        return e;
                    }
                    if ("findById".equals(name)) {
                        Long id = (Long) args[0];
                        if (storedEvent != null && storedEvent.getId().equals(id)) {
                            return Optional.of(storedEvent);
                        }
                        return Optional.empty();
                    }
                    if ("findAllByOrderByCreatedAtDesc".equals(name)) {
                        return List.of(storedEvent);
                    }
                    if ("findByStatusOrderByEventDateAsc".equals(name)) {
                        EventStatus status = (EventStatus) args[0];
                        if (storedEvent != null && storedEvent.getStatus() == status) {
                            return List.of(storedEvent);
                        }
                        return List.of();
                    }
                    if ("delete".equals(name)) {
                        storedEvent = null;
                        return null;
                    }
                    return null;
                }
        );

        registrationRepository = (RegistrationRepository) Proxy.newProxyInstance(
                RegistrationRepository.class.getClassLoader(),
                new Class<?>[]{RegistrationRepository.class},
                (proxy, method, args) -> {
                    if ("countByEventIdAndStatus".equals(method.getName())) {
                        return registrationCount;
                    }
                    return null;
                }
        );

        eventService = new EventServiceImpl(eventRepository, registrationRepository);
    }

    @Test
    void adminCreatesEventSuccessfullyAsDraft() {
        EventRequest request = EventRequest.builder()
                .title("AI Workshop")
                .description("Hands-on workshop")
                .venue("Lab 3")
                .eventDate(Instant.now().plus(15, ChronoUnit.DAYS))
                .registrationDeadline(Instant.now().plus(10, ChronoUnit.DAYS))
                .maxParticipants(50)
                .build();

        EventResponse response = eventService.createEvent(request);

        assertNotNull(response);
        assertEquals("AI Workshop", response.getTitle());
        assertEquals(EventStatus.DRAFT, response.getStatus());
        assertEquals(50, response.getMaxParticipants());
    }

    @Test
    void createEventFailsWhenDeadlineIsAfterEventDate() {
        EventRequest request = EventRequest.builder()
                .title("Bad Dates Event")
                .venue("Online")
                .eventDate(Instant.now().plus(5, ChronoUnit.DAYS))
                .registrationDeadline(Instant.now().plus(10, ChronoUnit.DAYS)) // deadline AFTER event date!
                .maxParticipants(50)
                .build();

        assertThrows(BadRequestException.class, () -> eventService.createEvent(request));
    }

    @Test
    void createEventFailsWhenMaxParticipantsIsZeroOrNegative() {
        EventRequest request = EventRequest.builder()
                .title("Zero Capacity Event")
                .venue("Hall")
                .eventDate(Instant.now().plus(10, ChronoUnit.DAYS))
                .registrationDeadline(Instant.now().plus(5, ChronoUnit.DAYS))
                .maxParticipants(0)
                .build();

        assertThrows(BadRequestException.class, () -> eventService.createEvent(request));
    }

    @Test
    void adminUpdatesEventSuccessfully() {
        EventRequest request = EventRequest.builder()
                .title("Tech Summit 2026 - Updated")
                .description("Updated desc")
                .venue("Main Hall")
                .eventDate(Instant.now().plus(40, ChronoUnit.DAYS))
                .registrationDeadline(Instant.now().plus(25, ChronoUnit.DAYS))
                .maxParticipants(120)
                .build();

        EventResponse response = eventService.updateEvent(1L, request);

        assertNotNull(response);
        assertEquals("Tech Summit 2026 - Updated", response.getTitle());
        assertEquals(120, response.getMaxParticipants());
    }

    @Test
    void updateEventFailsWhenReducingCapacityBelowExistingRegistrations() {
        registrationCount = 50; // 50 students registered

        EventRequest request = EventRequest.builder()
                .title("Tech Summit 2026")
                .venue("Hall")
                .eventDate(Instant.now().plus(30, ChronoUnit.DAYS))
                .registrationDeadline(Instant.now().plus(20, ChronoUnit.DAYS))
                .maxParticipants(30) // Attempting to lower to 30!
                .build();

        assertThrows(ConflictException.class, () -> eventService.updateEvent(1L, request));
    }

    @Test
    void adminPublishesDraftEvent() {
        EventResponse response = eventService.publishEvent(1L);

        assertNotNull(response);
        assertEquals(EventStatus.PUBLISHED, response.getStatus());
        assertEquals(EventStatus.PUBLISHED, storedEvent.getStatus());
    }

    @Test
    void adminCancelsEvent() {
        EventResponse response = eventService.cancelEvent(1L);

        assertNotNull(response);
        assertEquals(EventStatus.CANCELLED, response.getStatus());
        assertEquals(EventStatus.CANCELLED, storedEvent.getStatus());
    }

    @Test
    void adminDeletesDraftEvent() {
        eventService.deleteEvent(1L);
        assertThrows(ResourceNotFoundException.class, () -> eventService.getEventById(1L));
    }

    @Test
    void deleteFailsForPublishedEvent() {
        storedEvent.setStatus(EventStatus.PUBLISHED);

        assertThrows(ConflictException.class, () -> eventService.deleteEvent(1L));
    }

    @Test
    void studentOnlySeesPublishedEvents() {
        // Draft event is NOT returned in getPublishedEvents
        List<EventResponse> publicEvents = eventService.getPublishedEvents();
        assertTrue(publicEvents.isEmpty());

        // Publish event
        storedEvent.setStatus(EventStatus.PUBLISHED);
        publicEvents = eventService.getPublishedEvents();
        assertEquals(1, publicEvents.size());
        assertEquals("Tech Summit 2026", publicEvents.get(0).getTitle());
    }

    @Test
    void studentCannotViewDraftEventDetails() {
        // Stored event is DRAFT
        assertThrows(ResourceNotFoundException.class, () -> eventService.getPublishedEventById(1L));
    }
}
