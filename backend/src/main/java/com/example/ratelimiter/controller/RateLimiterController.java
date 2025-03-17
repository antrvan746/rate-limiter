package com.example.ratelimiter.controller;

import com.example.ratelimiter.service.RateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RateLimiterController {
    private final RateLimiterService rateLimiterService;

    @PostMapping("/posts")
    public ResponseEntity<String> createPost(@RequestHeader("X-User-Id") String userId) {
        if (!rateLimiterService.isAllowed(userId, "second")) {
            return ResponseEntity.status(429).body("Rate limit exceeded. Please try again later.");
        }
        return ResponseEntity.ok("Post created successfully");
    }

    @PostMapping("/accounts")
    public ResponseEntity<String> createAccount(@RequestHeader("X-IP-Address") String ipAddress) {
        if (!rateLimiterService.isAllowed(ipAddress, "day")) {
            return ResponseEntity.status(429).body("Daily account creation limit reached. Please try again tomorrow.");
        }
        return ResponseEntity.ok("Account created successfully");
    }

    @PostMapping("/rewards")
    public ResponseEntity<String> claimReward(@RequestHeader("X-Device-Id") String deviceId) {
        if (!rateLimiterService.isAllowed(deviceId, "week")) {
            return ResponseEntity.status(429).body("Weekly reward claim limit reached. Please try again next week.");
        }
        return ResponseEntity.ok("Reward claimed successfully");
    }
}