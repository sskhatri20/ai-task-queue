package com.taskqueue.aitaskqueue.controller.dto;

import com.taskqueue.aitaskqueue.model.JobStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class JobResponse {
    private String id;
    private JobStatus status;
    private String prompt;
    private int priority;
    private String result;
    private String errorMessage;
    private int retryCount;
    private Instant createdAt;
    private Instant updatedAt;
}
