package com.taskqueue.aitaskqueue.repository;

import com.taskqueue.aitaskqueue.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, String> {
    Optional<Job> findByIdempotencyKey(String idempotencyKey);
}
