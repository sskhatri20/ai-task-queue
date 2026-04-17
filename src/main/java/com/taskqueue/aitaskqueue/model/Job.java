package com.taskqueue.aitaskqueue.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(nullable = false)
    private int priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "retry_count")
    private int retryCount;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = updatedAt = Instant.now();
        if (status == null) status = JobStatus.PENDING;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
