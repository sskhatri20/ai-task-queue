package com.taskqueue.aitaskqueue.service;

import com.taskqueue.aitaskqueue.controller.dto.JobResponse;
import com.taskqueue.aitaskqueue.controller.dto.JobSubmitRequest;
import com.taskqueue.aitaskqueue.model.Job;
import com.taskqueue.aitaskqueue.model.JobStatus;
import com.taskqueue.aitaskqueue.producer.KafkaJobProducer;
import com.taskqueue.aitaskqueue.repository.JobRepository;
import com.taskqueue.aitaskqueue.websocket.JobStatusNotifier;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final KafkaJobProducer kafkaJobProducer;
    private final JobStatusNotifier jobStatusNotifier;
    private final MeterRegistry meterRegistry;

    @Transactional
    public JobResponse submitJob(JobSubmitRequest request) {
        if (request.getIdempotencyKey() != null) {
            Optional<Job> existing = jobRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                return toResponse(existing.get());
            }
        }

        Job job = Job.builder()
                .prompt(request.getPrompt())
                .priority(request.getPriority())
                .status(JobStatus.PENDING)
                .idempotencyKey(request.getIdempotencyKey())
                .retryCount(0)
                .build();
        job = jobRepository.save(job);
        kafkaJobProducer.send(job);
        meterRegistry.counter("jobs.submitted").increment();
        return toResponse(job);
    }

    @Transactional(readOnly = true)
    public Optional<JobResponse> getJob(String id) {
        return jobRepository.findById(id).map(this::toResponse);
    }

    @Transactional
    public void updateJobStatus(String id, JobStatus status, String result, String errorMessage) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus(status);
            if (result != null) job.setResult(result);
            if (errorMessage != null) job.setErrorMessage(errorMessage);
            jobRepository.save(job);
            jobStatusNotifier.notifyStatus(id, status);
        });
    }

    @Transactional
    public void incrementRetry(String id) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setRetryCount(job.getRetryCount() + 1);
            job.setStatus(JobStatus.RETRYING);
            jobRepository.save(job);
            jobStatusNotifier.notifyStatus(id, JobStatus.RETRYING);
        });
    }

    private JobResponse toResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .status(job.getStatus())
                .prompt(job.getPrompt())
                .priority(job.getPriority())
                .result(job.getResult())
                .errorMessage(job.getErrorMessage())
                .retryCount(job.getRetryCount())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}
