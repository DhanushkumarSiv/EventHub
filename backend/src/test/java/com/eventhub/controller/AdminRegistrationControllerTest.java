package com.eventhub.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Proxy;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.eventhub.dto.response.ApiResponse;
import com.eventhub.dto.response.RegistrationResponse;
import com.eventhub.service.RegistrationService;

class AdminRegistrationControllerTest {

    private RegistrationService registrationService;
    private AdminRegistrationController controller;

    @BeforeEach
    void setUp() {
        RegistrationResponse sample = RegistrationResponse.builder()
                .id(100L)
                .registrationCode("REG-12345")
                .userName("Participant A")
                .build();

        registrationService = (RegistrationService) Proxy.newProxyInstance(
                RegistrationService.class.getClassLoader(),
                new Class<?>[]{RegistrationService.class},
                (proxy, method, args) -> {
                    if ("getEventRegistrationsForAdmin".equals(method.getName())) {
                        return List.of(sample);
                    }
                    return null;
                }
        );

        controller = new AdminRegistrationController(registrationService);
    }

    @Test
    void getEventRegistrationsReturnsOkWithList() {
        ResponseEntity<ApiResponse<List<RegistrationResponse>>> response =
                controller.getEventRegistrations(1L, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("REG-12345", response.getBody().getData().get(0).getRegistrationCode());
    }
}
