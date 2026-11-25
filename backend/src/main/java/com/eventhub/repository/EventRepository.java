package com.eventhub.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eventhub.entity.Event;
import com.eventhub.entity.EventStatus;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatusOrderByEventDateAsc(EventStatus status);

    List<Event> findAllByOrderByCreatedAtDesc();

    long countByStatus(EventStatus status);

    List<Event> findTop5ByStatusAndEventDateAfterOrderByEventDateAsc(EventStatus status, Instant date);
}
