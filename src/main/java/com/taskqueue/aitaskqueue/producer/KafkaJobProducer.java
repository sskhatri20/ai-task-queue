package com.taskqueue.aitaskqueue.producer;

import com.taskqueue.aitaskqueue.model.Job;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaJobProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MeterRegistry meterRegistry;

    public void send(Job job) {
        KafkaJobMessage message = new KafkaJobMessage(job.getId(), job.getPrompt(), job.getPriority(), job.getRetryCount());
        kafkaTemplate.send("ai-jobs", job.getId(), message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish job {} to Kafka", job.getId(), ex);
                        meterRegistry.counter("kafka.send.failed").increment();
                    } else {
                        meterRegistry.counter("kafka.send.success").increment();
                    }
                });
    }

    public void sendRetry(KafkaJobMessage message) {
        kafkaTemplate.send("ai-jobs-retry", message.getJobId(), message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish retry for job {} to Kafka", message.getJobId(), ex);
                    }
                });
    }
}
