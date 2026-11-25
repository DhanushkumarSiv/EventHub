package com.eventhub.mapper;

import java.time.Instant;

import com.eventhub.dto.request.EventRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.EventStatus;

public final class EventMapper {

    private EventMapper() {
    }

    public static Event toEntity(EventRequest request) {
        if (request == null) {
            return null;
        }
        return Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .venue(request.getVenue())
                .eventDate(request.getEventDate())
                .registrationDeadline(request.getRegistrationDeadline())
                .maxParticipants(request.getMaxParticipants())
                .status(EventStatus.DRAFT)
                .build();
    }

    public static EventResponse toResponse(Event event, long registeredCount) {
        if (event == null) {
            return null;
        }
        Instant now = Instant.now();
        boolean isFull = event.getMaxParticipants() != null && registeredCount >= event.getMaxParticipants();
        boolean isClosed = event.getRegistrationDeadline() != null && now.isAfter(event.getRegistrationDeadline());

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .venue(event.getVenue())
                .eventDate(event.getEventDate())
                .registrationDeadline(event.getRegistrationDeadline())
                .maxParticipants(event.getMaxParticipants())
                .status(event.getStatus())
                .registeredCount(registeredCount)
                .full(isFull)
                .registrationClosed(isClosed)
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
