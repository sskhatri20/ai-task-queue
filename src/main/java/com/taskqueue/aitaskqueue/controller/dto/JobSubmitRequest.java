package com.taskqueue.aitaskqueue.controller.dto;

import lombok.Data;

@Data
public class JobSubmitRequest {
    private String prompt;
    private int priority = 5;
    private String idempotencyKey;
}
