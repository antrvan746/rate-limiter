package com.example.ratelimiter.service;

import com.example.ratelimiter.config.RateLimiterConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class RateLimiterService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimiterConfig config;

    public boolean isAllowed(String key, String type, int customLimit) {
        String redisKey = String.format("%s:%s", key, type);
        String currentCount = redisTemplate.opsForValue().get(redisKey);

        int limit = getLimit(type, customLimit);
        Duration duration = getDuration(type);

        if (currentCount == null) {
            redisTemplate.opsForValue().set(redisKey, "1", duration);
            return true;
        }

        int count = Integer.parseInt(currentCount);
        if (count >= limit) {
            return false;
        }

        redisTemplate.opsForValue().increment(redisKey);
        return true;
    }

    private int getLimit(String type, int customLimit) {
        if (customLimit > 0) {
            return customLimit;
        }

        return switch (type) {
            case "second" -> config.getMaxRequestsPerSecond();
            case "day" -> config.getMaxRequestsPerDay();
            case "week" -> config.getMaxRequestsPerWeek();
            default -> throw new IllegalArgumentException("Invalid rate limit type: " + type);
        };
    }

    private Duration getDuration(String type) {
        return switch (type) {
            case "second" -> Duration.of(1, ChronoUnit.SECONDS);
            case "day" -> Duration.of(1, ChronoUnit.DAYS);
            case "week" -> Duration.of(7, ChronoUnit.DAYS);
            default -> throw new IllegalArgumentException("Invalid rate limit type: " + type);
        };
    }
}