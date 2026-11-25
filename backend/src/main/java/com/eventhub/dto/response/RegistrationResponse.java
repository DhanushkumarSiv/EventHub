package com.eventhub.dto.response;

import java.time.Instant;

import com.eventhub.entity.AttendanceStatus;
import com.eventhub.entity.RegistrationStatus;

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
public class RegistrationResponse {

    private Long id;
    private String registrationCode;
    private Long eventId;
    private String eventTitle;
    private String venue;
    private Instant eventDate;
    private Long userId;
    private String userName;
    private String userEmail;
    private Instant registeredAt;
    private RegistrationStatus status;
    private AttendanceStatus attendanceStatus;
    private Instant attendanceTime;
}
