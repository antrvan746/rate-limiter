package com.example.ratelimiter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limiter.endpoints")
public class EndpointRateLimitConfig {
    private Map<String, RateLimitConfig> limits = new HashMap<>();

    @Data
    public static class RateLimitConfig {
        private String key;
        private String type;
        private int limit;
    }
}