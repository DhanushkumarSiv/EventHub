package com.eventhub.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.eventhub.dto.request.EventRequest;
import com.eventhub.dto.request.RegisterRequest;
import com.eventhub.entity.AttendanceStatus;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void registerRequestValidatesFields() {
        RegisterRequest invalid = RegisterRequest.builder()
                .name("")
                .email("not-an-email")
                .password("123") // too short
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(invalid);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));

        RegisterRequest valid = RegisterRequest.builder()
                .name("John Doe")
                .email("john@eventhub.com")
                .password("securePassword123")
                .build();
        assertTrue(validator.validate(valid).isEmpty());
    }

    @Test
    void eventRequestValidatesConstraints() {
        EventRequest invalid = EventRequest.builder()
                .title("")
                .venue("")
                .eventDate(Instant.now().minus(1, ChronoUnit.DAYS)) // in the past
                .registrationDeadline(null)
                .maxParticipants(0) // must be at least 1
                .build();

        Set<ConstraintViolation<EventRequest>> violations = validator.validate(invalid);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("eventDate")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("maxParticipants")));

        EventRequest valid = EventRequest.builder()
                .title("Workshop")
                .venue("Hall A")
                .eventDate(Instant.now().plus(10, ChronoUnit.DAYS))
                .registrationDeadline(Instant.now().plus(5, ChronoUnit.DAYS))
                .maxParticipants(50)
                .build();
        assertTrue(validator.validate(valid).isEmpty());
    }
}
