package com.example.ratelimiter.service;

import com.example.ratelimiter.config.RateLimiterConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RateLimiterService rateLimiterService;
    private RateLimiterConfig config;

    @BeforeEach
    void setUp() {
        config = new RateLimiterConfig();
        config.setMaxRequestsPerSecond(2);
        config.setMaxRequestsPerDay(10);
        config.setMaxRequestsPerWeek(5);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        rateLimiterService = new RateLimiterService(redisTemplate, config);
    }

    @Test
    void whenFirstRequest_shouldAllow() {
        // Given
        String key = "test-key";
        when(valueOperations.get(anyString())).thenReturn(null);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        boolean result = rateLimiterService.isAllowed(key, "second", 0);

        // Then
        assertTrue(result);
        verify(valueOperations).set(anyString(), eq("1"), any(Duration.class));
    }

    @Test
    void whenUnderLimit_shouldAllow() {
        // Given
        String key = "test-key";
        when(valueOperations.get(anyString())).thenReturn("1");
        when(valueOperations.increment(anyString())).thenReturn(2L);

        // When
        boolean result = rateLimiterService.isAllowed(key, "second", 3);

        // Then
        assertTrue(result);
        verify(valueOperations).increment(anyString());
    }

    @Test
    void whenAtLimit_shouldNotAllow() {
        // Given
        String key = "test-key";
        when(valueOperations.get(anyString())).thenReturn("3");

        // When
        boolean result = rateLimiterService.isAllowed(key, "second", 3);

        // Then
        assertFalse(result);
        verify(valueOperations, never()).increment(anyString());
    }

    @Test
    void whenCustomLimitProvided_shouldUseCustomLimit() {
        // Given
        String key = "test-key";
        when(valueOperations.get(anyString())).thenReturn("4");

        // When
        boolean result = rateLimiterService.isAllowed(key, "second", 5);

        // Then
        assertTrue(result);
        verify(valueOperations).increment(anyString());
    }

    @Test
    void whenInvalidType_shouldThrowException() {
        // Given
        String key = "test-key";

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> rateLimiterService.isAllowed(key, "invalid", 0));
    }
}