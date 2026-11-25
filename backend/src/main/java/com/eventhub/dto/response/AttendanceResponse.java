package com.eventhub.dto.response;

import java.time.Instant;

import com.eventhub.entity.AttendanceStatus;

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
public class AttendanceResponse {

    private Long id;
    private Long registrationId;
    private String registrationCode;
    private Long eventId;
    private String eventTitle;
    private Long attendeeId;
    private String attendeeName;
    private String attendeeEmail;
    private Long markedById;
    private String markedByName;
    private Instant attendanceTime;
    private AttendanceStatus status;
}
