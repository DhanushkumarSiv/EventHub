package com.eventhub.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.eventhub.dto.response.ApiResponse;
import com.eventhub.dto.response.RegistrationResponse;
import com.eventhub.entity.RegistrationStatus;
import com.eventhub.entity.UserRole;
import com.eventhub.security.UserPrincipal;
import com.eventhub.service.RegistrationService;

class RegistrationControllerTest {

    private RegistrationService registrationService;
    private RegistrationController registrationController;

    private UserPrincipal studentPrincipal;
    private RegistrationResponse sampleResponse;

    @BeforeEach
    void setUp() {
        studentPrincipal = UserPrincipal.builder()
                .id(10L)
                .name("Alice")
                .email("alice@eventhub.com")
                .role(UserRole.STUDENT)
                .build();

        sampleResponse = RegistrationResponse.builder()
                .id(100L)
                .registrationCode("REG-TESTCODE1")
                .eventId(1L)
                .eventTitle("Tech Conference")
                .userId(10L)
                .userName("Alice")
                .userEmail("alice@eventhub.com")
                .registeredAt(Instant.now())
                .status(RegistrationStatus.REGISTERED)
                .build();

        registrationService = (RegistrationService) Proxy.newProxyInstance(
                RegistrationService.class.getClassLoader(),
                new Class<?>[]{RegistrationService.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("registerForEvent".equals(name)) {
                        return sampleResponse;
                    }
                    if ("getMyRegistrations".equals(name)) {
                        return List.of(sampleResponse);
                    }
                    if ("getRegistrationById".equals(name)) {
                        return sampleResponse;
                    }
                    if ("cancelRegistration".equals(name)) {
                        return RegistrationResponse.builder()
                                .id(100L)
                                .status(RegistrationStatus.CANCELLED)
                                .build();
                    }
                    return null;
                }
        );

        registrationController = new RegistrationController(registrationService);
    }

    @Test
    void registerForEventReturnsCreated() {
        ResponseEntity<ApiResponse<RegistrationResponse>> response =
                registrationController.registerForEvent(1L, studentPrincipal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("REG-TESTCODE1", response.getBody().getData().getRegistrationCode());
    }

    @Test
    void getMyRegistrationsReturnsList() {
        ResponseEntity<ApiResponse<List<RegistrationResponse>>> response =
                registrationController.getMyRegistrations(studentPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void getRegistrationByIdReturnsDetails() {
        ResponseEntity<ApiResponse<RegistrationResponse>> response =
                registrationController.getRegistrationById(100L, studentPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().getData().getId());
    }

    @Test
    void cancelRegistrationReturnsCancelledStatus() {
        ResponseEntity<ApiResponse<RegistrationResponse>> response =
                registrationController.cancelRegistration(100L, studentPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(RegistrationStatus.CANCELLED, response.getBody().getData().getStatus());
    }
}
