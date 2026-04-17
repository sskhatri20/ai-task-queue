package com.taskqueue.aitaskqueue.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MeterRegistry meterRegistry;

    @Value("${app.rate-limit.max-requests:10}")
    private int maxRequests;

    @Value("${app.rate-limit.window-seconds:60}")
    private int windowSeconds;

    public boolean isAllowed(String clientId) {
        long now = Instant.now().toEpochMilli();
        long windowStart = now - (windowSeconds * 1000L);
        String key = "rate:" + clientId;

        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
        Long count = redisTemplate.opsForZSet().zCard(key);

        if (count != null && count >= maxRequests) {
            meterRegistry.counter("rate_limit.rejected", "client", clientId).increment();
            return false;
        }

        redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
        redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        return true;
    }
}
