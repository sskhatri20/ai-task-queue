package com.taskqueue.aitaskqueue.consumer;

import com.taskqueue.aitaskqueue.model.JobStatus;
import com.taskqueue.aitaskqueue.producer.KafkaJobMessage;
import com.taskqueue.aitaskqueue.producer.KafkaJobProducer;
import com.taskqueue.aitaskqueue.service.AiService;
import com.taskqueue.aitaskqueue.service.JobService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaJobConsumer {

    private static final int MAX_RETRIES = 3;

    private final JobService jobService;
    private final KafkaJobProducer kafkaJobProducer;
    private final AiService aiService;
    private final MeterRegistry meterRegistry;

    @KafkaListener(topics = {"ai-jobs", "ai-jobs-retry"}, groupId = "ai-workers",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(KafkaJobMessage message) {
        log.info("Processing job {}, attempt {}", message.getJobId(), message.getRetryCount());
        jobService.updateJobStatus(message.getJobId(), JobStatus.PROCESSING, null, null);
        meterRegistry.counter("jobs.processing").increment();

        try {
            String result = aiService.complete(message.getPrompt());
            jobService.updateJobStatus(message.getJobId(), JobStatus.COMPLETED, result, null);
            meterRegistry.counter("jobs.completed").increment();
            log.info("Job {} completed", message.getJobId());
        } catch (Exception e) {
            log.error("Job {} failed: {}", message.getJobId(), e.getMessage());
            meterRegistry.counter("jobs.failed").increment();

            if (message.getRetryCount() < MAX_RETRIES) {
                long backoffMs = (long) Math.pow(2, message.getRetryCount()) * 1000;
                log.info("Retrying job {} in {}ms (attempt {})", message.getJobId(), backoffMs, message.getRetryCount() + 1);
                sleepBackoff(backoffMs);
                jobService.incrementRetry(message.getJobId());
                kafkaJobProducer.sendRetry(new KafkaJobMessage(
                        message.getJobId(), message.getPrompt(), message.getPriority(), message.getRetryCount() + 1));
            } else {
                jobService.updateJobStatus(message.getJobId(), JobStatus.FAILED, null, e.getMessage());
                meterRegistry.counter("jobs.exhausted").increment();
                log.warn("Job {} exhausted retries", message.getJobId());
            }
        }
    }

    private void sleepBackoff(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
