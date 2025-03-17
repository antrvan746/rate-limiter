package com.example.ratelimiter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterConfig {
    private int maxRequestsPerSecond = 2;
    private int maxRequestsPerDay = 10;
    private int maxRequestsPerWeek = 5;
} 