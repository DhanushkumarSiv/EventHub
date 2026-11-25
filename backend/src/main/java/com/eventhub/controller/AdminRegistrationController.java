package com.eventhub.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eventhub.dto.response.ApiResponse;
import com.eventhub.dto.response.RegistrationResponse;
import com.eventhub.service.RegistrationService;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRegistrationController {

    private final RegistrationService registrationService;

    public AdminRegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/events/{id}/registrations")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> getEventRegistrations(
            @PathVariable Long id,
            @RequestParam(required = false) String search) {
        List<RegistrationResponse> registrations = registrationService.getEventRegistrationsForAdmin(id, search);
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }
}
