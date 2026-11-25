package com.eventhub.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventhub.dto.response.ApiResponse;
import com.eventhub.dto.response.RegistrationResponse;
import com.eventhub.entity.UserRole;
import com.eventhub.security.UserPrincipal;
import com.eventhub.service.RegistrationService;

@RestController
@RequestMapping("/api/v1")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/events/{id}/register")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<RegistrationResponse>> registerForEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        RegistrationResponse response = registrationService.registerForEvent(id, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Successfully registered for the event", response));
    }

    @GetMapping("/registrations/my")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> getMyRegistrations(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<RegistrationResponse> registrations = registrationService.getMyRegistrations(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }

    @GetMapping("/registrations/{id}")
    public ResponseEntity<ApiResponse<RegistrationResponse>> getRegistrationById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = principal.getRole() == UserRole.ADMIN;
        RegistrationResponse response = registrationService.getRegistrationById(id, principal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/registrations/{id}")
    public ResponseEntity<ApiResponse<RegistrationResponse>> cancelRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = principal.getRole() == UserRole.ADMIN;
        RegistrationResponse response = registrationService.cancelRegistration(id, principal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Registration cancelled successfully", response));
    }
}
