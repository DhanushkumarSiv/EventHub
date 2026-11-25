package com.eventhub.dto.response;

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
public class EventRegistrationStat {

    private Long eventId;
    private String eventTitle;
    private int maxParticipants;
    private long registeredCount;
    private long attendanceCount;
}
