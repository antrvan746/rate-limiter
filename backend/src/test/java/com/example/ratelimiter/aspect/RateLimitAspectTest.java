package com.example.ratelimiter.aspect;

import com.example.ratelimiter.annotation.RateLimit;
import com.example.ratelimiter.exception.RateLimitExceededException;
import com.example.ratelimiter.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private RateLimit rateLimit;

    private RateLimitAspect aspect;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        aspect = new RateLimitAspect(rateLimiterService);
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void whenRateLimitAllowed_shouldProceed() throws Throwable {
        // Given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn("success");
        when(rateLimit.key()).thenReturn("X-User-Id");
        when(rateLimit.type()).thenReturn("second");
        when(rateLimit.limit()).thenReturn(5);
        request.addHeader("X-User-Id", "user123");
        when(rateLimiterService.isAllowed(anyString(), anyString(), anyInt())).thenReturn(true);

        // When
        Object result = aspect.checkRateLimit(joinPoint, rateLimit);

        // Then
        assertNotNull(result);
        assertEquals("success", result);
        verify(rateLimiterService).isAllowed("user123", "second", 5);
    }

    @Test
    void whenRateLimitExceeded_shouldThrowException() {
        // Given
        when(rateLimit.key()).thenReturn("X-User-Id");
        when(rateLimit.type()).thenReturn("second");
        when(rateLimit.limit()).thenReturn(5);
        request.addHeader("X-User-Id", "user123");
        when(rateLimiterService.isAllowed(anyString(), anyString(), anyInt())).thenReturn(false);

        // When/Then
        assertThrows(RateLimitExceededException.class,
                () -> aspect.checkRateLimit(mock(ProceedingJoinPoint.class), rateLimit));
    }

    @Test
    void whenHeaderMissing_shouldThrowException() {
        // Given
        when(rateLimit.key()).thenReturn("X-User-Id");

        // When/Then
        assertThrows(IllegalArgumentException.class,
                () -> aspect.checkRateLimit(mock(ProceedingJoinPoint.class), rateLimit));
    }
}