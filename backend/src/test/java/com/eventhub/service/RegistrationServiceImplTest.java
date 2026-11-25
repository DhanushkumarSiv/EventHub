package com.eventhub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eventhub.dto.response.RegistrationResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.EventStatus;
import com.eventhub.entity.Registration;
import com.eventhub.entity.RegistrationStatus;
import com.eventhub.entity.User;
import com.eventhub.entity.UserRole;
import com.eventhub.exception.BadRequestException;
import com.eventhub.exception.ConflictException;
import com.eventhub.exception.ForbiddenException;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.service.impl.RegistrationServiceImpl;

class RegistrationServiceImplTest {

    private RegistrationRepository registrationRepository;
    private EventRepository eventRepository;
    private UserRepository userRepository;
    private RegistrationServiceImpl registrationService;

    private User student1;
    private User student2;
    private Event publishedEvent;
    private Registration storedRegistration;
    private long activeRegistrationCount = 0;

    @BeforeEach
    void setUp() {
        student1 = User.builder().id(10L).name("Alice").email("alice@eventhub.com").role(UserRole.STUDENT).build();
        student2 = User.builder().id(20L).name("Bob").email("bob@eventhub.com").role(UserRole.STUDENT).build();

        publishedEvent = Event.builder()
                .id(1L)
                .title("Spring Boot Workshop")
                .venue("Lab 1")
                .eventDate(Instant.now().plus(10, ChronoUnit.DAYS))
                .registrationDeadline(Instant.now().plus(5, ChronoUnit.DAYS))
                .maxParticipants(50)
                .status(EventStatus.PUBLISHED)
                .build();

        storedRegistration = null;
        activeRegistrationCount = 5;

        userRepository = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        Long id = (Long) args[0];
                        if (id.equals(10L)) return Optional.of(student1);
                        if (id.equals(20L)) return Optional.of(student2);
                        return Optional.empty();
                    }
                    return null;
                }
        );

        eventRepository = (EventRepository) Proxy.newProxyInstance(
                EventRepository.class.getClassLoader(),
                new Class<?>[]{EventRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        Long id = (Long) args[0];
                        if (id.equals(1L)) return Optional.of(publishedEvent);
                        return Optional.empty();
                    }
                    if ("existsById".equals(method.getName())) {
                        Long id = (Long) args[0];
                        return id.equals(1L);
                    }
                    return null;
                }
        );

        registrationRepository = (RegistrationRepository) Proxy.newProxyInstance(
                RegistrationRepository.class.getClassLoader(),
                new Class<?>[]{RegistrationRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        Registration r = (Registration) args[0];
                        if (r.getId() == null) {
                            r.setId(100L);
                        }
                        storedRegistration = r;
                        return r;
                    }
                    if ("findByUserIdAndEventId".equals(name)) {
                        Long userId = (Long) args[0];
                        Long eventId = (Long) args[1];
                        if (storedRegistration != null &&
                            storedRegistration.getUser().getId().equals(userId) &&
                            storedRegistration.getEvent().getId().equals(eventId)) {
                            return Optional.of(storedRegistration);
                        }
                        return Optional.empty();
                    }
                    if ("findByRegistrationCode".equals(name)) {
                        return Optional.empty(); // No collisions
                    }
                    if ("countByEventIdAndStatus".equals(name)) {
                        return activeRegistrationCount;
                    }
                    if ("findByUserIdWithDetails".equals(name)) {
                        Long userId = (Long) args[0];
                        if (storedRegistration != null && storedRegistration.getUser().getId().equals(userId)) {
                            return List.of(storedRegistration);
                        }
                        return List.of();
                    }
                    if ("findByIdWithDetails".equals(name)) {
                        Long id = (Long) args[0];
                        if (storedRegistration != null && storedRegistration.getId().equals(id)) {
                            return Optional.of(storedRegistration);
                        }
                        return Optional.empty();
                    }
                    if ("findByEventIdWithDetails".equals(name)) {
                        if (storedRegistration != null) {
                            return List.of(storedRegistration);
                        }
                        return List.of();
                    }
                    return null;
                }
        );

        registrationService = new RegistrationServiceImpl(registrationRepository, eventRepository, userRepository);
    }

    @Test
    void studentRegistersSuccessfully() {
        RegistrationResponse response = registrationService.registerForEvent(1L, 10L);

        assertNotNull(response);
        assertEquals(1L, response.getEventId());
        assertEquals(10L, response.getUserId());
        assertEquals(RegistrationStatus.REGISTERED, response.getStatus());
        assertNotNull(response.getRegistrationCode());
    }

    @Test
    void duplicateRegistrationIsRejected() {
        // First registration
        registrationService.registerForEvent(1L, 10L);

        // Duplicate registration attempt by same user for same event
        assertThrows(ConflictException.class, () -> registrationService.registerForEvent(1L, 10L));
    }

    @Test
    void registrationForUnpublishedEventIsRejected() {
        publishedEvent.setStatus(EventStatus.DRAFT);

        assertThrows(BadRequestException.class, () -> registrationService.registerForEvent(1L, 10L));
    }

    @Test
    void registrationAfterDeadlineIsRejected() {
        publishedEvent.setRegistrationDeadline(Instant.now().minus(1, ChronoUnit.DAYS));

        assertThrows(BadRequestException.class, () -> registrationService.registerForEvent(1L, 10L));
    }

    @Test
    void registrationWhenCapacityIsFullIsRejected() {
        activeRegistrationCount = 50; // maxParticipants is 50

        assertThrows(ConflictException.class, () -> registrationService.registerForEvent(1L, 10L));
    }

    @Test
    void studentViewsTheirRegistrations() {
        registrationService.registerForEvent(1L, 10L);

        List<RegistrationResponse> myRegistrations = registrationService.getMyRegistrations(10L);
        assertEquals(1, myRegistrations.size());
        assertEquals("Spring Boot Workshop", myRegistrations.get(0).getEventTitle());
    }

    @Test
    void studentCannotAccessAnotherStudentsRegistration() {
        registrationService.registerForEvent(1L, 10L); // Registered for student1 (id 10)

        // Student2 (id 20) attempts to access student1's registration
        assertThrows(ForbiddenException.class, () ->
                registrationService.getRegistrationById(100L, 20L, false));
    }

    @Test
    void adminCanAccessAnyRegistration() {
        registrationService.registerForEvent(1L, 10L);

        // Admin (isAdmin = true) accesses registration
        RegistrationResponse response = registrationService.getRegistrationById(100L, 99L, true);
        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    void studentCancelsRegistrationSuccessfully() {
        registrationService.registerForEvent(1L, 10L);

        RegistrationResponse response = registrationService.cancelRegistration(100L, 10L, false);
        assertEquals(RegistrationStatus.CANCELLED, response.getStatus());
    }

    @Test
    void cancellingAlreadyCancelledRegistrationThrowsBadRequest() {
        registrationService.registerForEvent(1L, 10L);
        registrationService.cancelRegistration(100L, 10L, false);

        assertThrows(BadRequestException.class, () ->
                registrationService.cancelRegistration(100L, 10L, false));
    }

    @Test
    void adminSearchesEventRegistrations() {
        registrationService.registerForEvent(1L, 10L);

        // Search matching Alice
        List<RegistrationResponse> results = registrationService.getEventRegistrationsForAdmin(1L, "alice");
        assertEquals(1, results.size());

        // Search non-matching
        List<RegistrationResponse> noResults = registrationService.getEventRegistrationsForAdmin(1L, "nonexistent");
        assertEquals(0, noResults.size());
    }
}
