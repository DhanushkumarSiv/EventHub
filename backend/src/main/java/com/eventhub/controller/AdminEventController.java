package com.eventhub.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventhub.dto.request.EventRequest;
import com.eventhub.dto.response.ApiResponse;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.service.EventService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/events")
@PreAuthorize("hasRole('ADMIN')")
public class AdminEventController {

    private final EventService eventService;

    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(@Valid @RequestBody EventRequest request) {
        EventResponse response = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event created successfully as DRAFT", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        List<EventResponse> events = eventService.getAllEventsForAdmin();
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable Long id) {
        EventResponse response = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {
        EventResponse response = eventService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", response));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<EventResponse>> publishEvent(@PathVariable Long id) {
        EventResponse response = eventService.publishEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event published successfully", response));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<EventResponse>> cancelEvent(@PathVariable Long id) {
        EventResponse response = eventService.cancelEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event cancelled successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Draft event deleted successfully", null));
    }
}
