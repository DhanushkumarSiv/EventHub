package com.eventhub.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.eventhub.dto.request.EventRequest;
import com.eventhub.dto.response.ApiResponse;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.entity.EventStatus;
import com.eventhub.service.EventService;

class AdminEventControllerTest {

    private EventService eventService;
    private AdminEventController adminEventController;

    private EventResponse sampleDraftEvent;
    private EventResponse samplePublishedEvent;
    private EventResponse sampleCancelledEvent;

    @BeforeEach
    void setUp() {
        sampleDraftEvent = EventResponse.builder()
                .id(1L)
                .title("Draft Event")
                .venue("Hall A")
                .eventDate(Instant.now().plus(20, ChronoUnit.DAYS))
                .registrationDeadline(Instant.now().plus(10, ChronoUnit.DAYS))
                .status(EventStatus.DRAFT)
                .maxParticipants(50)
                .build();

        samplePublishedEvent = EventResponse.builder()
                .id(1L)
                .title("Draft Event")
                .status(EventStatus.PUBLISHED)
                .build();

        sampleCancelledEvent = EventResponse.builder()
                .id(1L)
                .title("Draft Event")
                .status(EventStatus.CANCELLED)
                .build();

        eventService = (EventService) Proxy.newProxyInstance(
                EventService.class.getClassLoader(),
                new Class<?>[]{EventService.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("createEvent".equals(name)) {
                        return sampleDraftEvent;
                    }
                    if ("getAllEventsForAdmin".equals(name)) {
                        return List.of(sampleDraftEvent);
                    }
                    if ("getEventById".equals(name)) {
                        return sampleDraftEvent;
                    }
                    if ("updateEvent".equals(name)) {
                        EventRequest req = (EventRequest) args[1];
                        return EventResponse.builder()
                                .id((Long) args[0])
                                .title(req.getTitle())
                                .status(EventStatus.DRAFT)
                                .build();
                    }
                    if ("publishEvent".equals(name)) {
                        return samplePublishedEvent;
                    }
                    if ("cancelEvent".equals(name)) {
                        return sampleCancelledEvent;
                    }
                    if ("deleteEvent".equals(name)) {
                        return null;
                    }
                    return null;
                }
        );

        adminEventController = new AdminEventController(eventService);
    }

    @Test
    void createEventReturnsCreatedWithDraftEvent() {
        EventRequest request = EventRequest.builder()
                .title("Draft Event")
                .venue("Hall A")
                .eventDate(Instant.now().plus(20, ChronoUnit.DAYS))
                .registrationDeadline(Instant.now().plus(10, ChronoUnit.DAYS))
                .maxParticipants(50)
                .build();

        ResponseEntity<ApiResponse<EventResponse>> response = adminEventController.createEvent(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Draft Event", response.getBody().getData().getTitle());
        assertEquals(EventStatus.DRAFT, response.getBody().getData().getStatus());
    }

    @Test
    void getAllEventsReturnsAllEvents() {
        ResponseEntity<ApiResponse<List<EventResponse>>> response = adminEventController.getAllEvents();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void updateEventReturnsUpdatedDetails() {
        EventRequest request = EventRequest.builder()
                .title("Updated Title")
                .venue("Hall B")
                .eventDate(Instant.now().plus(25, ChronoUnit.DAYS))
                .registrationDeadline(Instant.now().plus(15, ChronoUnit.DAYS))
                .maxParticipants(80)
                .build();

        ResponseEntity<ApiResponse<EventResponse>> response = adminEventController.updateEvent(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Title", response.getBody().getData().getTitle());
    }

    @Test
    void publishEventReturnsPublishedStatus() {
        ResponseEntity<ApiResponse<EventResponse>> response = adminEventController.publishEvent(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(EventStatus.PUBLISHED, response.getBody().getData().getStatus());
    }

    @Test
    void cancelEventReturnsCancelledStatus() {
        ResponseEntity<ApiResponse<EventResponse>> response = adminEventController.cancelEvent(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(EventStatus.CANCELLED, response.getBody().getData().getStatus());
    }

    @Test
    void deleteEventReturnsOk() {
        ResponseEntity<ApiResponse<Void>> response = adminEventController.deleteEvent(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }
}
