package com.eventhub.dto.response;

import java.time.Instant;

import com.eventhub.entity.EventStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private String venue;
    private Instant eventDate;
    private Instant registrationDeadline;
    private Integer maxParticipants;
    private EventStatus status;
    private long registeredCount;
    private boolean full;
    private boolean registrationClosed;
    private Instant createdAt;
    private Instant updatedAt;
}
