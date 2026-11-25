package com.eventhub.mapper;

import com.eventhub.dto.response.AttendanceResponse;
import com.eventhub.dto.response.RegistrationResponse;
import com.eventhub.entity.Attendance;
import com.eventhub.entity.Registration;

public final class RegistrationMapper {

    private RegistrationMapper() {
    }

    public static RegistrationResponse toResponse(Registration registration) {
        if (registration == null) {
            return null;
        }

        RegistrationResponse.RegistrationResponseBuilder builder = RegistrationResponse.builder()
                .id(registration.getId())
                .registrationCode(registration.getRegistrationCode())
                .registeredAt(registration.getRegisteredAt())
                .status(registration.getStatus());

        if (registration.getEvent() != null) {
            builder.eventId(registration.getEvent().getId())
                   .eventTitle(registration.getEvent().getTitle())
                   .venue(registration.getEvent().getVenue())
                   .eventDate(registration.getEvent().getEventDate());
        }

        if (registration.getUser() != null) {
            builder.userId(registration.getUser().getId())
                   .userName(registration.getUser().getName())
                   .userEmail(registration.getUser().getEmail());
        }

        if (registration.getAttendance() != null) {
            builder.attendanceStatus(registration.getAttendance().getStatus())
                   .attendanceTime(registration.getAttendance().getAttendanceTime());
        }

        return builder.build();
    }

    public static AttendanceResponse toAttendanceResponse(Attendance attendance) {
        if (attendance == null) {
            return null;
        }

        AttendanceResponse.AttendanceResponseBuilder builder = AttendanceResponse.builder()
                .id(attendance.getId())
                .attendanceTime(attendance.getAttendanceTime())
                .status(attendance.getStatus());

        if (attendance.getRegistration() != null) {
            builder.registrationId(attendance.getRegistration().getId())
                   .registrationCode(attendance.getRegistration().getRegistrationCode());

            if (attendance.getRegistration().getEvent() != null) {
                builder.eventId(attendance.getRegistration().getEvent().getId())
                       .eventTitle(attendance.getRegistration().getEvent().getTitle());
            }

            if (attendance.getRegistration().getUser() != null) {
                builder.attendeeId(attendance.getRegistration().getUser().getId())
                       .attendeeName(attendance.getRegistration().getUser().getName())
                       .attendeeEmail(attendance.getRegistration().getUser().getEmail());
            }
        }

        if (attendance.getMarkedBy() != null) {
            builder.markedById(attendance.getMarkedBy().getId())
                   .markedByName(attendance.getMarkedBy().getName());
        }

        return builder.build();
    }
}
