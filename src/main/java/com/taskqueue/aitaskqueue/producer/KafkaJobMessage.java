package com.taskqueue.aitaskqueue.producer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaJobMessage {
    private String jobId;
    private String prompt;
    private int priority;
    private int retryCount;
}
