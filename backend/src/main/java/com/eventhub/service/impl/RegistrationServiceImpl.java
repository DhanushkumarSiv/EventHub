package com.eventhub.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.eventhub.dto.response.RegistrationResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.EventStatus;
import com.eventhub.entity.Registration;
import com.eventhub.entity.RegistrationStatus;
import com.eventhub.entity.User;
import com.eventhub.exception.BadRequestException;
import com.eventhub.exception.ConflictException;
import com.eventhub.exception.ForbiddenException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.mapper.RegistrationMapper;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.service.RegistrationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public RegistrationServiceImpl(
            RegistrationRepository registrationRepository,
            EventRepository eventRepository,
            UserRepository userRepository) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public RegistrationResponse registerForEvent(Long eventId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        // 1. Business rule: Event must be PUBLISHED
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BadRequestException("Registration is not available. The event is " + event.getStatus());
        }

        Instant now = Instant.now();

        // 2. Business rule: Registration deadline must not have passed
        if (event.getRegistrationDeadline() != null && now.isAfter(event.getRegistrationDeadline())) {
            throw new BadRequestException("Registration for this event has closed. The deadline has passed.");
        }

        // 3. Business rule: Event must not have already taken place
        if (event.getEventDate() != null && now.isAfter(event.getEventDate())) {
            throw new BadRequestException("Cannot register for an event that has already occurred");
        }

        // 4. Business rule: Check for duplicate registration
        var existingOpt = registrationRepository.findByUserIdAndEventId(userId, eventId);
        if (existingOpt.isPresent()) {
            Registration existing = existingOpt.get();
            if (existing.getStatus() == RegistrationStatus.REGISTERED) {
                throw new ConflictException("You are already registered for this event");
            }
            // If previously cancelled, check capacity and reactivate
            checkCapacity(event);
            existing.setStatus(RegistrationStatus.REGISTERED);
            existing.setRegisteredAt(now);
            Registration reactivated = registrationRepository.save(existing);
            log.info("Reactivated registration {} for user {} on event {}", reactivated.getRegistrationCode(), userId, eventId);
            return RegistrationMapper.toResponse(reactivated);
        }

        // 5. Business rule: Check event capacity
        checkCapacity(event);

        // 6. Generate unique registration code and save
        String registrationCode = generateUniqueRegistrationCode();

        Registration registration = Registration.builder()
                .registrationCode(registrationCode)
                .user(user)
                .event(event)
                .registeredAt(now)
                .status(RegistrationStatus.REGISTERED)
                .build();

        Registration saved = registrationRepository.save(registration);
        log.info("Successfully registered user {} for event {} with code {}", userId, eventId, registrationCode);

        return RegistrationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getMyRegistrations(Long userId) {
        return registrationRepository.findByUserIdWithDetails(userId).stream()
                .map(RegistrationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationResponse getRegistrationById(Long registrationId, Long requestingUserId, boolean isAdmin) {
        Registration registration = registrationRepository.findByIdWithDetails(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", registrationId));

        if (!isAdmin && !registration.getUser().getId().equals(requestingUserId)) {
            throw new ForbiddenException("You do not have permission to view this registration");
        }

        return RegistrationMapper.toResponse(registration);
    }

    @Override
    @Transactional
    public RegistrationResponse cancelRegistration(Long registrationId, Long requestingUserId, boolean isAdmin) {
        Registration registration = registrationRepository.findByIdWithDetails(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", registrationId));

        if (!isAdmin && !registration.getUser().getId().equals(requestingUserId)) {
            throw new ForbiddenException("You do not have permission to cancel this registration");
        }

        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new BadRequestException("Registration is already cancelled");
        }

        if (registration.getEvent() != null && registration.getEvent().getEventDate() != null) {
            if (Instant.now().isAfter(registration.getEvent().getEventDate())) {
                throw new BadRequestException("Cannot cancel registration for an event that has already occurred");
            }
        }

        registration.setStatus(RegistrationStatus.CANCELLED);
        Registration saved = registrationRepository.save(registration);
        log.info("Cancelled registration {} by user {}", registrationId, requestingUserId);

        return RegistrationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getEventRegistrationsForAdmin(Long eventId, String search) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event", "id", eventId);
        }

        List<Registration> registrations = registrationRepository.findByEventIdWithDetails(eventId);

        if (StringUtils.hasText(search)) {
            String query = search.trim().toLowerCase();
            return registrations.stream()
                    .filter(r -> (r.getUser() != null && (
                            r.getUser().getName().toLowerCase().contains(query) ||
                            r.getUser().getEmail().toLowerCase().contains(query)
                    )) || r.getRegistrationCode().toLowerCase().contains(query))
                    .map(RegistrationMapper::toResponse)
                    .toList();
        }

        return registrations.stream()
                .map(RegistrationMapper::toResponse)
                .toList();
    }

    private void checkCapacity(Event event) {
        long activeCount = registrationRepository.countByEventIdAndStatus(event.getId(), RegistrationStatus.REGISTERED);
        if (event.getMaxParticipants() != null && activeCount >= event.getMaxParticipants()) {
            throw new ConflictException("Event capacity of " + event.getMaxParticipants() + " has been reached");
        }
    }

    private String generateUniqueRegistrationCode() {
        String code;
        do {
            code = "REG-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        } while (registrationRepository.findByRegistrationCode(code).isPresent());
        return code;
    }
}
