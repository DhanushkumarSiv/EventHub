package com.eventhub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.eventhub.entity.Attendance;
import com.eventhub.entity.AttendanceStatus;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByRegistrationId(Long registrationId);

    boolean existsByRegistrationId(Long registrationId);

    long countByStatus(AttendanceStatus status);

    @Query("SELECT COUNT(a) FROM Attendance a " +
           "WHERE a.registration.event.id = :eventId AND a.status = :status")
    long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") AttendanceStatus status);
}
