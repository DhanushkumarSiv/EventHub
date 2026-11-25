package com.eventhub.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.eventhub.dto.response.ApiResponse;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.entity.EventStatus;
import com.eventhub.service.EventService;

class EventControllerTest {

    private EventService eventService;
    private EventController eventController;

    @BeforeEach
    void setUp() {
        EventResponse sampleEvent = EventResponse.builder()
                .id(1L)
                .title("Public Hackathon")
                .venue("Campus Hall")
                .eventDate(Instant.now().plusSeconds(86400))
                .status(EventStatus.PUBLISHED)
                .maxParticipants(100)
                .registeredCount(10)
                .build();

        eventService = (EventService) Proxy.newProxyInstance(
                EventService.class.getClassLoader(),
                new Class<?>[]{EventService.class},
                (proxy, method, args) -> {
                    if ("getPublishedEvents".equals(method.getName())) {
                        return List.of(sampleEvent);
                    }
                    if ("getPublishedEventById".equals(method.getName())) {
                        return sampleEvent;
                    }
                    return null;
                }
        );

        eventController = new EventController(eventService);
    }

    @Test
    void getPublishedEventsReturnsOkWithList() {
        ResponseEntity<ApiResponse<List<EventResponse>>> response = eventController.getPublishedEvents();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Public Hackathon", response.getBody().getData().get(0).getTitle());
    }

    @Test
    void getEventByIdReturnsOkWithEvent() {
        ResponseEntity<ApiResponse<EventResponse>> response = eventController.getEventById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1L, response.getBody().getData().getId());
        assertEquals("Public Hackathon", response.getBody().getData().getTitle());
    }
}
