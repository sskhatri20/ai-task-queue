package com.taskqueue.aitaskqueue.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic jobsTopic() {
        return TopicBuilder.name("ai-jobs").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic jobsRetryTopic() {
        return TopicBuilder.name("ai-jobs-retry").partitions(3).replicas(1).build();
    }
}
