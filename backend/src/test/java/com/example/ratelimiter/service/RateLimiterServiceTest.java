package com.example.ratelimiter.service;

import com.example.ratelimiter.config.RateLimiterConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
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
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        rateLimiterService = new RateLimiterService(redisTemplate, config);
    }

    @Test
    void shouldAllowFirstRequest() {
        // Given
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        boolean result = rateLimiterService.isAllowed("test-key", "second");

        // Then
        assertThat(result).isTrue();
        verify(redisTemplate).expire(anyString(), any());
    }

    @Test
    void shouldBlockWhenLimitExceeded() {
        // Given
        when(valueOperations.increment(anyString())).thenReturn(3L);

        // When
        boolean result = rateLimiterService.isAllowed("test-key", "second");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldHandleDifferentTimeWindows() {
        // Given
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        boolean resultSecond = rateLimiterService.isAllowed("test-key", "second");
        boolean resultDay = rateLimiterService.isAllowed("test-key", "day");
        boolean resultWeek = rateLimiterService.isAllowed("test-key", "week");

        // Then
        assertThat(resultSecond).isTrue();
        assertThat(resultDay).isTrue();
        assertThat(resultWeek).isTrue();
    }

    @Test
    void shouldUseCorrectRedisKey() {
        // Given
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        rateLimiterService.isAllowed("test-key", "second");

        // Then
        verify(valueOperations).increment("rate-limit:second:test-key");
    }
}