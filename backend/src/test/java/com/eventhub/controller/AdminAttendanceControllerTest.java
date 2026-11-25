package com.eventhub.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Proxy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.eventhub.dto.request.AttendanceRequest;
import com.eventhub.dto.response.ApiResponse;
import com.eventhub.dto.response.AttendanceResponse;
import com.eventhub.entity.AttendanceStatus;
import com.eventhub.entity.UserRole;
import com.eventhub.security.UserPrincipal;
import com.eventhub.service.AttendanceService;

class AdminAttendanceControllerTest {

    private AttendanceService attendanceService;
    private AdminAttendanceController controller;
    private UserPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        adminPrincipal = UserPrincipal.builder()
                .id(99L)
                .name("Admin")
                .role(UserRole.ADMIN)
                .build();

        AttendanceResponse sample = AttendanceResponse.builder()
                .id(500L)
                .registrationId(100L)
                .status(AttendanceStatus.PRESENT)
                .build();

        attendanceService = (AttendanceService) Proxy.newProxyInstance(
                AttendanceService.class.getClassLoader(),
                new Class<?>[]{AttendanceService.class},
                (proxy, method, args) -> {
                    if ("markAttendance".equals(method.getName())) {
                        return sample;
                    }
                    return null;
                }
        );

        controller = new AdminAttendanceController(attendanceService);
    }

    @Test
    void markAttendanceReturnsOkWithAttendanceResponse() {
        AttendanceRequest req = AttendanceRequest.builder().status(AttendanceStatus.PRESENT).build();
        ResponseEntity<ApiResponse<AttendanceResponse>> response =
                controller.markAttendance(100L, req, adminPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(AttendanceStatus.PRESENT, response.getBody().getData().getStatus());
    }
}
