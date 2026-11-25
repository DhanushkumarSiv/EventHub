package com.eventhub.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.eventhub.dto.request.LoginRequest;
import com.eventhub.dto.request.RegisterRequest;
import com.eventhub.dto.response.ApiResponse;
import com.eventhub.dto.response.AuthResponse;
import com.eventhub.dto.response.UserSummaryResponse;
import com.eventhub.entity.UserRole;
import com.eventhub.security.UserPrincipal;
import com.eventhub.service.AuthService;

class AuthControllerTest {

    private AuthController authController;

    @BeforeEach
    void setUp() {
        AuthService fakeAuthService = new AuthService() {
            @Override
            public AuthResponse register(RegisterRequest request) {
                return AuthResponse.builder()
                        .token("jwt-mock-token")
                        .user(UserSummaryResponse.builder()
                                .id(1L)
                                .name(request.getName())
                                .email(request.getEmail())
                                .role(UserRole.STUDENT)
                                .build())
                        .build();
            }

            @Override
            public AuthResponse login(LoginRequest request) {
                return AuthResponse.builder()
                        .token("admin-jwt-token")
                        .user(UserSummaryResponse.builder()
                                .id(99L)
                                .name("Admin User")
                                .email(request.getEmail())
                                .role(UserRole.ADMIN)
                                .build())
                        .build();
            }

            @Override
            public UserSummaryResponse getCurrentUser(UserPrincipal principal) {
                return UserSummaryResponse.builder()
                        .id(principal.getId())
                        .name(principal.getName())
                        .email(principal.getEmail())
                        .role(principal.getRole())
                        .build();
            }
        };

        authController = new AuthController(fakeAuthService);
    }

    @Test
    void registerReturnsCreatedResponse() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Student One")
                .email("student1@eventhub.com")
                .password("Password@123")
                .build();

        ResponseEntity<ApiResponse<AuthResponse>> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("jwt-mock-token", response.getBody().getData().getToken());
        assertEquals("student1@eventhub.com", response.getBody().getData().getUser().getEmail());
    }

    @Test
    void loginReturnsOkResponse() {
        LoginRequest request = LoginRequest.builder()
                .email("admin@eventhub.com")
                .password("Admin@1234")
                .build();

        ResponseEntity<ApiResponse<AuthResponse>> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("admin-jwt-token", response.getBody().getData().getToken());
        assertEquals("admin@eventhub.com", response.getBody().getData().getUser().getEmail());
    }

    @Test
    void getCurrentUserReturnsUserSummary() {
        UserPrincipal principal = UserPrincipal.builder()
                .id(1L)
                .email("student1@eventhub.com")
                .name("Student One")
                .role(UserRole.STUDENT)
                .build();

        ResponseEntity<ApiResponse<UserSummaryResponse>> response = authController.getCurrentUser(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("student1@eventhub.com", response.getBody().getData().getEmail());
    }
}
