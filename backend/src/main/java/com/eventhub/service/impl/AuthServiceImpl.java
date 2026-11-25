package com.eventhub.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventhub.dto.request.LoginRequest;
import com.eventhub.dto.request.RegisterRequest;
import com.eventhub.dto.response.AuthResponse;
import com.eventhub.dto.response.UserSummaryResponse;
import com.eventhub.entity.User;
import com.eventhub.entity.UserRole;
import com.eventhub.exception.ConflictException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.exception.UnauthorizedException;
import com.eventhub.mapper.UserMapper;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.JwtUtils;
import com.eventhub.security.UserPrincipal;
import com.eventhub.service.AuthService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("An account with email '" + normalizedEmail + "' already exists");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.STUDENT) // Public registration is strictly restricted to STUDENT
                .build();

        User savedUser = userRepository.save(user);
        log.info("Registered new student with id: {} and email: {}", savedUser.getId(), savedUser.getEmail());

        String token = jwtUtils.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(UserMapper.toSummaryResponse(savedUser))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

            String token = jwtUtils.generateToken(principal);
            log.info("User logged in successfully: {}", normalizedEmail);

            return AuthResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .user(UserSummaryResponse.builder()
                            .id(principal.getId())
                            .name(principal.getName())
                            .email(principal.getEmail())
                            .role(principal.getRole())
                            .build())
                    .build();
        } catch (BadCredentialsException ex) {
            log.warn("Failed login attempt for email: {}", normalizedEmail);
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse getCurrentUser(UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        return UserMapper.toSummaryResponse(user);
    }
}
