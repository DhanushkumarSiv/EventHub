package com.eventhub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Proxy;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.eventhub.dto.request.LoginRequest;
import com.eventhub.dto.request.RegisterRequest;
import com.eventhub.dto.response.AuthResponse;
import com.eventhub.dto.response.UserSummaryResponse;
import com.eventhub.entity.User;
import com.eventhub.entity.UserRole;
import com.eventhub.exception.ConflictException;
import com.eventhub.exception.UnauthorizedException;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.JwtUtils;
import com.eventhub.security.UserPrincipal;
import com.eventhub.service.impl.AuthServiceImpl;

class AuthServiceImplTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtUtils jwtUtils;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtUtils = new JwtUtils("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970", 3600000);

        userRepository = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> {
                    if ("existsByEmail".equals(method.getName())) {
                        String email = (String) args[0];
                        return "existing@eventhub.com".equalsIgnoreCase(email);
                    }
                    if ("save".equals(method.getName())) {
                        User user = (User) args[0];
                        user.setId(1L);
                        return user;
                    }
                    if ("findById".equals(method.getName())) {
                        Long id = (Long) args[0];
                        User user = User.builder()
                                .id(id)
                                .name("Alice Smith")
                                .email("alice@eventhub.com")
                                .role(UserRole.STUDENT)
                                .build();
                        return Optional.of(user);
                    }
                    return null;
                }
        );

        authenticationManager = (AuthenticationManager) Proxy.newProxyInstance(
                AuthenticationManager.class.getClassLoader(),
                new Class<?>[]{AuthenticationManager.class},
                (proxy, method, args) -> {
                    if ("authenticate".equals(method.getName())) {
                        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) args[0];
                        if ("wrong@eventhub.com".equals(token.getName()) || "WrongPassword".equals(token.getCredentials())) {
                            throw new BadCredentialsException("Bad credentials");
                        }
                        User user = User.builder()
                                .id(1L)
                                .name("Alice Smith")
                                .email((String) token.getName())
                                .password("encodedPassword")
                                .role(UserRole.STUDENT)
                                .build();
                        UserPrincipal principal = UserPrincipal.create(user);
                        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    }
                    return null;
                }
        );

        authService = new AuthServiceImpl(userRepository, passwordEncoder, authenticationManager, jwtUtils);
    }

    @Test
    void registerStudentSuccessfully() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alice Smith")
                .email("alice@eventhub.com")
                .password("Password@123")
                .build();

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("alice@eventhub.com", response.getUser().getEmail());
        assertEquals(UserRole.STUDENT, response.getUser().getRole());
    }

    @Test
    void registerThrowsConflictExceptionWhenEmailAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Duplicate User")
                .email("existing@eventhub.com")
                .password("Password@123")
                .build();

        assertThrows(ConflictException.class, () -> authService.register(request));
    }

    @Test
    void loginSuccessfullyReturnsToken() {
        LoginRequest request = LoginRequest.builder()
                .email("alice@eventhub.com")
                .password("Password@123")
                .build();

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("alice@eventhub.com", response.getUser().getEmail());
        assertEquals(UserRole.STUDENT, response.getUser().getRole());
    }

    @Test
    void loginThrowsUnauthorizedExceptionWhenCredentialsAreInvalid() {
        LoginRequest request = LoginRequest.builder()
                .email("wrong@eventhub.com")
                .password("WrongPassword")
                .build();

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void getCurrentUserReturnsUserSummary() {
        User user = User.builder()
                .id(1L)
                .name("Alice Smith")
                .email("alice@eventhub.com")
                .role(UserRole.STUDENT)
                .build();
        UserPrincipal principal = UserPrincipal.create(user);

        UserSummaryResponse response = authService.getCurrentUser(principal);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("alice@eventhub.com", response.getEmail());
    }
}
