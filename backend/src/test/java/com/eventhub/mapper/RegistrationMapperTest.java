package com.eventhub.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.eventhub.dto.response.AttendanceResponse;
import com.eventhub.dto.response.RegistrationResponse;
import com.eventhub.entity.Attendance;
import com.eventhub.entity.AttendanceStatus;
import com.eventhub.entity.Event;
import com.eventhub.entity.Registration;
import com.eventhub.entity.RegistrationStatus;
import com.eventhub.entity.User;
import com.eventhub.entity.UserRole;

class RegistrationMapperTest {

    @Test
    void toResponseMapsRegistrationWithAttendance() {
        User user = User.builder().id(1L).name("Bob").email("bob@eventhub.com").role(UserRole.STUDENT).build();
        Event event = Event.builder().id(2L).title("Workshop").venue("Lab 1").eventDate(Instant.now()).build();
        Registration registration = Registration.builder()
                .id(3L)
                .registrationCode("REG-12345")
                .user(user)
                .event(event)
                .registeredAt(Instant.now())
                .status(RegistrationStatus.REGISTERED)
                .build();

        Attendance attendance = Attendance.builder()
                .id(4L)
                .registration(registration)
                .markedBy(user)
                .attendanceTime(Instant.now())
                .status(AttendanceStatus.PRESENT)
                .build();
        registration.setAttendance(attendance);

        RegistrationResponse response = RegistrationMapper.toResponse(registration);

        assertNotNull(response);
        assertEquals(3L, response.getId());
        assertEquals("REG-12345", response.getRegistrationCode());
        assertEquals(2L, response.getEventId());
        assertEquals("Workshop", response.getEventTitle());
        assertEquals("Bob", response.getUserName());
        assertEquals(AttendanceStatus.PRESENT, response.getAttendanceStatus());
    }

    @Test
    void toAttendanceResponseMapsAttendanceCorrectly() {
        User admin = User.builder().id(99L).name("Admin User").role(UserRole.ADMIN).build();
        User student = User.builder().id(5L).name("Charlie").email("charlie@eventhub.com").build();
        Event event = Event.builder().id(10L).title("Seminar").build();

        Registration registration = Registration.builder()
                .id(20L)
                .registrationCode("REG-777")
                .user(student)
                .event(event)
                .build();

        Attendance attendance = Attendance.builder()
                .id(30L)
                .registration(registration)
                .markedBy(admin)
                .attendanceTime(Instant.now())
                .status(AttendanceStatus.PRESENT)
                .build();

        AttendanceResponse response = RegistrationMapper.toAttendanceResponse(attendance);

        assertNotNull(response);
        assertEquals(30L, response.getId());
        assertEquals(20L, response.getRegistrationId());
        assertEquals("REG-777", response.getRegistrationCode());
        assertEquals("Charlie", response.getAttendeeName());
        assertEquals("Admin User", response.getMarkedByName());
        assertEquals(AttendanceStatus.PRESENT, response.getStatus());
    }
}
