package com.eventhub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eventhub.dto.request.AttendanceRequest;
import com.eventhub.dto.response.AttendanceResponse;
import com.eventhub.entity.Attendance;
import com.eventhub.entity.AttendanceStatus;
import com.eventhub.entity.Event;
import com.eventhub.entity.EventStatus;
import com.eventhub.entity.Registration;
import com.eventhub.entity.RegistrationStatus;
import com.eventhub.entity.User;
import com.eventhub.entity.UserRole;
import com.eventhub.exception.BadRequestException;
import com.eventhub.exception.ConflictException;
import com.eventhub.repository.AttendanceRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.service.impl.AttendanceServiceImpl;

class AttendanceServiceImplTest {

    private AttendanceRepository attendanceRepository;
    private RegistrationRepository registrationRepository;
    private UserRepository userRepository;
    private AttendanceServiceImpl attendanceService;

    private User admin;
    private User student;
    private Event event;
    private Registration registration;
    private Attendance storedAttendance;

    @BeforeEach
    void setUp() {
        admin = User.builder().id(99L).name("Admin").email("admin@eventhub.com").role(UserRole.ADMIN).build();
        student = User.builder().id(10L).name("Alice").email("alice@eventhub.com").role(UserRole.STUDENT).build();
        event = Event.builder().id(1L).title("Workshop").status(EventStatus.PUBLISHED).build();
        registration = Registration.builder()
                .id(100L)
                .registrationCode("REG-TEST1")
                .user(student)
                .event(event)
                .status(RegistrationStatus.REGISTERED)
                .build();
        storedAttendance = null;

        userRepository = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        Long id = (Long) args[0];
                        if (id.equals(99L)) return Optional.of(admin);
                        return Optional.empty();
                    }
                    return null;
                }
        );

        registrationRepository = (RegistrationRepository) Proxy.newProxyInstance(
                RegistrationRepository.class.getClassLoader(),
                new Class<?>[]{RegistrationRepository.class},
                (proxy, method, args) -> {
                    if ("findByIdWithDetails".equals(method.getName())) {
                        Long id = (Long) args[0];
                        if (id.equals(100L)) return Optional.of(registration);
                        return Optional.empty();
                    }
                    return null;
                }
        );

        attendanceRepository = (AttendanceRepository) Proxy.newProxyInstance(
                AttendanceRepository.class.getClassLoader(),
                new Class<?>[]{AttendanceRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findByRegistrationId".equals(name)) {
                        return Optional.ofNullable(storedAttendance);
                    }
                    if ("save".equals(name)) {
                        Attendance a = (Attendance) args[0];
                        if (a.getId() == null) a.setId(500L);
                        storedAttendance = a;
                        return a;
                    }
                    return null;
                }
        );

        attendanceService = new AttendanceServiceImpl(attendanceRepository, registrationRepository, userRepository);
    }

    @Test
    void adminMarksAttendanceAsPresent() {
        AttendanceRequest request = AttendanceRequest.builder().status(AttendanceStatus.PRESENT).build();

        AttendanceResponse response = attendanceService.markAttendance(100L, request, 99L);

        assertNotNull(response);
        assertEquals(500L, response.getId());
        assertEquals(100L, response.getRegistrationId());
        assertEquals("REG-TEST1", response.getRegistrationCode());
        assertEquals(AttendanceStatus.PRESENT, response.getStatus());
        assertEquals("Admin", response.getMarkedByName());
    }

    @Test
    void adminUpdatesAttendanceFromPresentToAbsent() {
        // Initial attendance PRESENT
        AttendanceRequest reqPresent = AttendanceRequest.builder().status(AttendanceStatus.PRESENT).build();
        attendanceService.markAttendance(100L, reqPresent, 99L);

        // Update to ABSENT
        AttendanceRequest reqAbsent = AttendanceRequest.builder().status(AttendanceStatus.ABSENT).build();
        AttendanceResponse response = attendanceService.markAttendance(100L, reqAbsent, 99L);

        assertNotNull(response);
        assertEquals(AttendanceStatus.ABSENT, response.getStatus());
    }

    @Test
    void markingAttendanceFailsForCancelledRegistration() {
        registration.setStatus(RegistrationStatus.CANCELLED);
        AttendanceRequest request = AttendanceRequest.builder().status(AttendanceStatus.PRESENT).build();

        assertThrows(ConflictException.class, () -> attendanceService.markAttendance(100L, request, 99L));
    }

    @Test
    void markingAttendanceFailsForDraftEvent() {
        event.setStatus(EventStatus.DRAFT);
        AttendanceRequest request = AttendanceRequest.builder().status(AttendanceStatus.PRESENT).build();

        assertThrows(BadRequestException.class, () -> attendanceService.markAttendance(100L, request, 99L));
    }

    @Test
    void getAttendanceByRegistrationIdReturnsDetails() {
        AttendanceRequest reqPresent = AttendanceRequest.builder().status(AttendanceStatus.PRESENT).build();
        attendanceService.markAttendance(100L, reqPresent, 99L);

        AttendanceResponse response = attendanceService.getAttendanceByRegistrationId(100L);
        assertNotNull(response);
        assertEquals(AttendanceStatus.PRESENT, response.getStatus());
    }
}
