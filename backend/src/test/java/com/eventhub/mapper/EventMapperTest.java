package com.eventhub.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import com.eventhub.dto.request.EventRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.EventStatus;

class EventMapperTest {

    @Test
    void toEntityMapsEventRequest() {
        EventRequest request = EventRequest.builder()
                .title("Tech Conference")
                .description("Annual event")
                .venue("Main Hall")
                .eventDate(Instant.now().plus(10, ChronoUnit.DAYS))
                .registrationDeadline(Instant.now().plus(5, ChronoUnit.DAYS))
                .maxParticipants(100)
                .build();

        Event event = EventMapper.toEntity(request);

        assertNotNull(event);
        assertEquals("Tech Conference", event.getTitle());
        assertEquals(EventStatus.DRAFT, event.getStatus());
        assertEquals(100, event.getMaxParticipants());
    }

    @Test
    void toResponseComputesCapacityAndDeadlineFlags() {
        Event event = Event.builder()
                .id(10L)
                .title("Tech Conference")
                .venue("Hall B")
                .eventDate(Instant.now().plus(10, ChronoUnit.DAYS))
                .registrationDeadline(Instant.now().minus(1, ChronoUnit.DAYS)) // already passed
                .maxParticipants(50)
                .status(EventStatus.PUBLISHED)
                .build();

        EventResponse response = EventMapper.toResponse(event, 50);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(50L, response.getRegisteredCount());
        assertTrue(response.isFull(), "Should be full when registeredCount equals maxParticipants");
        assertTrue(response.isRegistrationClosed(), "Should be closed when deadline is in the past");
    }

    @Test
    void toResponseReportsOpenWhenUnderCapacity() {
        Event event = Event.builder()
                .id(11L)
                .title("Hackathon")
                .venue("Virtual")
                .eventDate(Instant.now().plus(10, ChronoUnit.DAYS))
                .registrationDeadline(Instant.now().plus(5, ChronoUnit.DAYS))
                .maxParticipants(100)
                .status(EventStatus.PUBLISHED)
                .build();

        EventResponse response = EventMapper.toResponse(event, 25);

        assertNotNull(response);
        assertFalse(response.isFull());
        assertFalse(response.isRegistrationClosed());
    }
}
