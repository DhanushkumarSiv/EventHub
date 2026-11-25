package com.eventhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.eventhub.entity.Registration;
import com.eventhub.entity.RegistrationStatus;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    Optional<Registration> findByRegistrationCode(String registrationCode);

    Optional<Registration> findByUserIdAndEventId(Long userId, Long eventId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    boolean existsByUserIdAndEventIdAndStatus(Long userId, Long eventId, RegistrationStatus status);

    List<Registration> findByUserIdOrderByRegisteredAtDesc(Long userId);

    List<Registration> findByEventIdOrderByRegisteredAtDesc(Long eventId);

    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);

    long countByStatus(RegistrationStatus status);

    @Query("SELECT r FROM Registration r " +
           "JOIN FETCH r.user " +
           "JOIN FETCH r.event " +
           "LEFT JOIN FETCH r.attendance " +
           "WHERE r.id = :id")
    Optional<Registration> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM Registration r " +
           "JOIN FETCH r.user " +
           "LEFT JOIN FETCH r.attendance " +
           "WHERE r.event.id = :eventId " +
           "ORDER BY r.registeredAt DESC")
    List<Registration> findByEventIdWithDetails(@Param("eventId") Long eventId);

    @Query("SELECT r FROM Registration r " +
           "JOIN FETCH r.event " +
           "LEFT JOIN FETCH r.attendance " +
           "WHERE r.user.id = :userId " +
           "ORDER BY r.registeredAt DESC")
    List<Registration> findByUserIdWithDetails(@Param("userId") Long userId);
}
