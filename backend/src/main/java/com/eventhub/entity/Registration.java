package com.eventhub.entity;

import java.time.Instant;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "registrations",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_registrations_user_event", columnNames = {"user_id", "event_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_code", nullable = false, unique = true, length = 64)
    private String registrationCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    @OneToOne(mappedBy = "registration", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Attendance attendance;

    @PrePersist
    public void prePersist() {
        if (this.registeredAt == null) {
            this.registeredAt = Instant.now();
        }
        if (this.status == null) {
            this.status = RegistrationStatus.REGISTERED;
        }
    }
}
