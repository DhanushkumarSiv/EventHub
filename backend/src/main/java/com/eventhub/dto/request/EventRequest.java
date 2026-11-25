package com.eventhub.dto.request;

import java.time.Instant;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class EventRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private String description;

    @NotBlank(message = "Venue is required")
    @Size(max = 200, message = "Venue must not exceed 200 characters")
    private String venue;

    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    private Instant eventDate;

    @NotNull(message = "Registration deadline is required")
    private Instant registrationDeadline;

    @NotNull(message = "Maximum participants is required")
    @Min(value = 1, message = "Maximum participants must be at least 1")
    private Integer maxParticipants;
}
