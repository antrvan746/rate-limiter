package com.example.ratelimiter.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class RateLimiterIntegrationTest {

        @Container
        static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.2"))
                        .withExposedPorts(6379);

        @AfterAll
        static void tearDown() {
                redis.close();
        }

        @DynamicPropertySource
        static void redisProperties(DynamicPropertyRegistry registry) {
                registry.add("spring.data.redis.host", redis::getHost);
                registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        }

        @Autowired
        private TestRestTemplate restTemplate;

        private String baseUrl;

        @BeforeEach
        void setUp() {
                baseUrl = "http://localhost:"
                                + restTemplate.getRestTemplate().getUriTemplateHandler().expand("/").getPort();
        }

        @Test
        void whenRateLimitNotExceeded_shouldAllowRequest() {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-User-Id", "user123");

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/api/posts",
                                HttpMethod.POST,
                                new HttpEntity<>(headers),
                                String.class);

                assertThat(response.getStatusCode().value()).isEqualTo(200);
                assertThat(response.getBody()).isEqualTo("Post created successfully");
        }

        @Test
        void whenRateLimitExceeded_shouldReturn429() {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-User-Id", "user123");

                // Make requests up to the limit
                for (int i = 0; i < 5; i++) {
                        ResponseEntity<String> response = restTemplate.exchange(
                                        baseUrl + "/api/posts",
                                        HttpMethod.POST,
                                        new HttpEntity<>(headers),
                                        String.class);
                        assertThat(response.getStatusCode().value()).isEqualTo(200);
                }

                // Next request should be rate limited
                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/api/posts",
                                HttpMethod.POST,
                                new HttpEntity<>(headers),
                                String.class);

                assertThat(response.getStatusCode().value()).isEqualTo(429);
                assertThat(response.getBody()).isEqualTo("Rate limit exceeded for second");
        }

        @Test
        void whenHeaderMissing_shouldReturn400() {
                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/api/posts",
                                HttpMethod.POST,
                                new HttpEntity<>(new HttpHeaders()),
                                String.class);

                assertThat(response.getStatusCode().value()).isEqualTo(400);
                assertThat(response.getBody()).isEqualTo("Required header X-User-Id is missing");
        }

        @Test
        void whenDifferentEndpoints_shouldHaveDifferentLimits() {
                // Test posts endpoint (limit: 5 per second)
                HttpHeaders postHeaders = new HttpHeaders();
                postHeaders.set("X-User-Id", "user123");
                for (int i = 0; i < 5; i++) {
                        ResponseEntity<String> response = restTemplate.exchange(
                                        baseUrl + "/api/posts",
                                        HttpMethod.POST,
                                        new HttpEntity<>(postHeaders),
                                        String.class);
                        assertThat(response.getStatusCode().value()).isEqualTo(200);
                }
                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/api/posts",
                                HttpMethod.POST,
                                new HttpEntity<>(postHeaders),
                                String.class);
                assertThat(response.getStatusCode().value()).isEqualTo(429);

                // Test accounts endpoint (limit: 3 per day)
                HttpHeaders accountHeaders = new HttpHeaders();
                accountHeaders.set("X-IP-Address", "192.168.1.1");
                for (int i = 0; i < 3; i++) {
                        response = restTemplate.exchange(
                                        baseUrl + "/api/accounts",
                                        HttpMethod.POST,
                                        new HttpEntity<>(accountHeaders),
                                        String.class);
                        assertThat(response.getStatusCode().value()).isEqualTo(200);
                }
                response = restTemplate.exchange(
                                baseUrl + "/api/accounts",
                                HttpMethod.POST,
                                new HttpEntity<>(accountHeaders),
                                String.class);
                assertThat(response.getStatusCode().value()).isEqualTo(429);

                // Test rewards endpoint (limit: 1 per week)
                HttpHeaders rewardHeaders = new HttpHeaders();
                rewardHeaders.set("X-Device-Id", "device123");
                response = restTemplate.exchange(
                                baseUrl + "/api/rewards",
                                HttpMethod.POST,
                                new HttpEntity<>(rewardHeaders),
                                String.class);
                assertThat(response.getStatusCode().value()).isEqualTo(200);
                response = restTemplate.exchange(
                                baseUrl + "/api/rewards",
                                HttpMethod.POST,
                                new HttpEntity<>(rewardHeaders),
                                String.class);
                assertThat(response.getStatusCode().value()).isEqualTo(429);
        }
}