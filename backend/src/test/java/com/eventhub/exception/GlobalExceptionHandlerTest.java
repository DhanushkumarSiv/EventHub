package com.eventhub.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.eventhub.dto.response.ErrorResponse;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test");
    }

    @Test
    void handleResourceNotFoundReturns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Event", "id", 42L);
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Event not found with id: '42'", response.getBody().getMessage());
        assertEquals("/api/v1/test", response.getBody().getPath());
    }

    @Test
    void handleConflictReturns409() {
        ConflictException ex = new ConflictException("Email already exists");
        ResponseEntity<ErrorResponse> response = handler.handleConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Email already exists", response.getBody().getMessage());
    }

    @Test
    void handleBadRequestReturns400() {
        BadRequestException ex = new BadRequestException("Registration deadline must be before event date");
        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Registration deadline must be before event date", response.getBody().getMessage());
    }

    @Test
    void handleUnauthorizedReturns401() {
        UnauthorizedException ex = new UnauthorizedException("Invalid credentials");
        ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    void handleForbiddenReturns403() {
        ForbiddenException ex = new ForbiddenException("Access denied for role STUDENT");
        ResponseEntity<ErrorResponse> response = handler.handleForbidden(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Access denied for role STUDENT", response.getBody().getMessage());
    }
}
