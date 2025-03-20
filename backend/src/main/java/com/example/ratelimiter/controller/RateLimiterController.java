package com.example.ratelimiter.controller;

import com.example.ratelimiter.annotation.RateLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RateLimiterController {

    @PostMapping("/posts")
    @RateLimit(key = "X-User-Id", type = "second", limit = 5)
    public ResponseEntity<String> createPost(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok("Post created successfully");
    }

    @PostMapping("/accounts")
    @RateLimit(key = "X-IP-Address", type = "day", limit = 3)
    public ResponseEntity<String> createAccount(@RequestHeader("X-IP-Address") String ipAddress) {
        return ResponseEntity.ok("Account created successfully");
    }

    @PostMapping("/rewards")
    @RateLimit(key = "X-Device-Id", type = "week", limit = 1)
    public ResponseEntity<String> claimReward(@RequestHeader("X-Device-Id") String deviceId) {
        return ResponseEntity.ok("Reward claimed successfully");
    }
}