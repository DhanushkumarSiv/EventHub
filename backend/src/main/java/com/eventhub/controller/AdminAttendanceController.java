package com.eventhub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventhub.dto.request.AttendanceRequest;
import com.eventhub.dto.response.ApiResponse;
import com.eventhub.dto.response.AttendanceResponse;
import com.eventhub.security.UserPrincipal;
import com.eventhub.service.AttendanceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAttendanceController {

    private final AttendanceService attendanceService;

    public AdminAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PatchMapping("/registrations/{id}/attendance")
    public ResponseEntity<ApiResponse<AttendanceResponse>> markAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceRequest request,
            @AuthenticationPrincipal UserPrincipal adminPrincipal) {
        AttendanceResponse response = attendanceService.markAttendance(id, request, adminPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Attendance updated successfully", response));
    }
}
