package com.example.ratelimiter.service;

import com.example.ratelimiter.config.RateLimiterConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RateLimiterService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimiterConfig config;

    public boolean isAllowed(String key, String type) {
        String redisKey = getRedisKey(key, type);
        Long count = redisTemplate.opsForValue().increment(redisKey);
        
        if (count == 1) {
            // Set expiration based on type
            Duration expiration = getExpirationDuration(type);
            redisTemplate.expire(redisKey, expiration);
        }

        return count <= getMaxRequests(type);
    }

    private String getRedisKey(String key, String type) {
        return "rate-limit:" + type + ":" + key;
    }

    private Duration getExpirationDuration(String type) {
        return switch (type.toLowerCase()) {
            case "second" -> Duration.ofSeconds(1);
            case "day" -> Duration.ofDays(1);
            case "week" -> Duration.ofDays(7);
            default -> Duration.ofSeconds(1);
        };
    }

    private int getMaxRequests(String type) {
        return switch (type.toLowerCase()) {
            case "second" -> config.getMaxRequestsPerSecond();
            case "day" -> config.getMaxRequestsPerDay();
            case "week" -> config.getMaxRequestsPerWeek();
            default -> config.getMaxRequestsPerSecond();
        };
    }
} 