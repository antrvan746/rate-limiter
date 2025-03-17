package com.example.ratelimiter.controller;

import com.example.ratelimiter.service.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimiterControllerTest {

    @Mock
    private RateLimiterService rateLimiterService;

    private RateLimiterController controller;

    @BeforeEach
    void setUp() {
        controller = new RateLimiterController(rateLimiterService);
    }

    @Test
    void shouldAllowPostCreation() {
        // Given
        when(rateLimiterService.isAllowed(anyString(), anyString())).thenReturn(true);

        // When
        ResponseEntity<String> response = controller.createPost("test-user");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Post created successfully");
    }

    @Test
    void shouldBlockPostCreationWhenRateLimitExceeded() {
        // Given
        when(rateLimiterService.isAllowed(anyString(), anyString())).thenReturn(false);

        // When
        ResponseEntity<String> response = controller.createPost("test-user");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isEqualTo("Rate limit exceeded. Please try again later.");
    }

    @Test
    void shouldAllowAccountCreation() {
        // Given
        when(rateLimiterService.isAllowed(anyString(), anyString())).thenReturn(true);

        // When
        ResponseEntity<String> response = controller.createAccount("192.168.1.1");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Account created successfully");
    }

    @Test
    void shouldBlockAccountCreationWhenRateLimitExceeded() {
        // Given
        when(rateLimiterService.isAllowed(anyString(), anyString())).thenReturn(false);

        // When
        ResponseEntity<String> response = controller.createAccount("192.168.1.1");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isEqualTo("Daily account creation limit reached. Please try again tomorrow.");
    }

    @Test
    void shouldAllowRewardClaim() {
        // Given
        when(rateLimiterService.isAllowed(anyString(), anyString())).thenReturn(true);

        // When
        ResponseEntity<String> response = controller.claimReward("device-123");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Reward claimed successfully");
    }

    @Test
    void shouldBlockRewardClaimWhenRateLimitExceeded() {
        // Given
        when(rateLimiterService.isAllowed(anyString(), anyString())).thenReturn(false);

        // When
        ResponseEntity<String> response = controller.claimReward("device-123");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isEqualTo("Weekly reward claim limit reached. Please try again next week.");
    }
}