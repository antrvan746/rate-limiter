package com.example.ratelimiter.integration;

import com.example.ratelimiter.RateLimiterApplication;
import com.example.ratelimiter.service.RateLimiterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = RateLimiterApplication.class)
@Testcontainers
class RateLimiterIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Test
    void shouldEnforceRateLimits() {
        // Test post creation rate limit
        String userId = "test-user-1";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        for (int i = 0; i < 2; i++) {
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/posts",
                    HttpMethod.POST,
                    entity,
                    String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        // This request should be blocked
        ResponseEntity<String> blockedResponse = restTemplate.exchange(
                "/api/posts",
                HttpMethod.POST,
                entity,
                String.class);
        assertThat(blockedResponse.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void shouldHandleDifferentRateLimitTypes() {
        // Test account creation (daily limit)
        String ipAddress = "192.168.1.1";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-IP-Address", ipAddress);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        for (int i = 0; i < 10; i++) {
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/accounts",
                    HttpMethod.POST,
                    entity,
                    String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        // This request should be blocked
        ResponseEntity<String> blockedResponse = restTemplate.exchange(
                "/api/accounts",
                HttpMethod.POST,
                entity,
                String.class);
        assertThat(blockedResponse.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void shouldResetRateLimitsAfterExpiration() throws InterruptedException {
        // Test reward claim (weekly limit)
        String deviceId = "device-123";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Device-Id", deviceId);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        for (int i = 0; i < 5; i++) {
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/rewards",
                    HttpMethod.POST,
                    entity,
                    String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        // This request should be blocked
        ResponseEntity<String> blockedResponse = restTemplate.exchange(
                "/api/rewards",
                HttpMethod.POST,
                entity,
                String.class);
        assertThat(blockedResponse.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        // Wait for the rate limit to expire (in a real test, you might want to mock the
        // time)
        Thread.sleep(7000); // Wait for 7 seconds (for the second-based rate limit)

        // This request should be allowed again
        HttpHeaders newHeaders = new HttpHeaders();
        newHeaders.set("X-User-Id", "new-user");
        HttpEntity<String> newEntity = new HttpEntity<>(null, newHeaders);
        ResponseEntity<String> allowedResponse = restTemplate.exchange(
                "/api/posts",
                HttpMethod.POST,
                newEntity,
                String.class);
        assertThat(allowedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}