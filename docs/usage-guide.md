# Rate Limiter Usage Guide

This guide provides detailed instructions on how to use the Rate Limiter service in your Spring Boot application.

## Basic Usage

### 1. Add Rate Limit to Endpoint

```java
@RestController
@RequestMapping("/api")
public class YourController {
    
    @PostMapping("/resource")
    @RateLimit(key = "X-User-Id", type = "second", limit = 10)
    public ResponseEntity<String> yourEndpoint() {
        return ResponseEntity.ok("Success");
    }
}
```

### 2. Make Requests with Headers

```bash
curl -X POST http://your-api/resource \
  -H "X-User-Id: user123"
```

## Advanced Usage

### 1. Multiple Rate Limits

```java
@RestController
@RequestMapping("/api")
public class YourController {
    
    @PostMapping("/resource")
    @RateLimit(key = "X-User-Id", type = "second", limit = 5)
    @RateLimit(key = "X-IP-Address", type = "day", limit = 100)
    public ResponseEntity<String> yourEndpoint() {
        return ResponseEntity.ok("Success");
    }
}
```

### 2. Different Rate Limit Types

```java
@RestController
@RequestMapping("/api")
public class YourController {
    
    // Second-based rate limit
    @PostMapping("/posts")
    @RateLimit(key = "X-User-Id", type = "second", limit = 5)
    public ResponseEntity<String> createPost() {
        return ResponseEntity.ok("Post created");
    }
    
    // Day-based rate limit
    @PostMapping("/accounts")
    @RateLimit(key = "X-IP-Address", type = "day", limit = 3)
    public ResponseEntity<String> createAccount() {
        return ResponseEntity.ok("Account created");
    }
    
    // Week-based rate limit
    @PostMapping("/rewards")
    @RateLimit(key = "X-Device-Id", type = "week", limit = 1)
    public ResponseEntity<String> claimReward() {
        return ResponseEntity.ok("Reward claimed");
    }
}
```

### 3. Custom Rate Limit Keys

```java
@RestController
@RequestMapping("/api")
public class YourController {
    
    @PostMapping("/resource")
    @RateLimit(key = "X-Custom-Key", type = "second", limit = 10)
    public ResponseEntity<String> yourEndpoint() {
        return ResponseEntity.ok("Success");
    }
}
```

## Error Handling

### 1. Rate Limit Exceeded

```java
@ExceptionHandler(RateLimitExceededException.class)
public ResponseEntity<String> handleRateLimitExceeded(RateLimitExceededException e) {
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(e.getMessage());
}
```

### 2. Missing Headers

```java
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(e.getMessage());
}
```

## Common Use Cases

### 1. API Rate Limiting

```java
@RestController
@RequestMapping("/api")
public class ApiController {
    
    @GetMapping("/public")
    @RateLimit(key = "X-IP-Address", type = "second", limit = 10)
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("Public API");
    }
    
    @GetMapping("/private")
    @RateLimit(key = "X-User-Id", type = "second", limit = 5)
    public ResponseEntity<String> privateEndpoint() {
        return ResponseEntity.ok("Private API");
    }
}
```

### 2. User Action Rate Limiting

```java
@RestController
@RequestMapping("/api")
public class UserController {
    
    @PostMapping("/login")
    @RateLimit(key = "X-IP-Address", type = "second", limit = 3)
    public ResponseEntity<String> login() {
        return ResponseEntity.ok("Login successful");
    }
    
    @PostMapping("/password-reset")
    @RateLimit(key = "X-IP-Address", type = "hour", limit = 1)
    public ResponseEntity<String> resetPassword() {
        return ResponseEntity.ok("Password reset email sent");
    }
}
```

### 3. Resource Creation Rate Limiting

```java
@RestController
@RequestMapping("/api")
public class ResourceController {
    
    @PostMapping("/posts")
    @RateLimit(key = "X-User-Id", type = "second", limit = 5)
    public ResponseEntity<String> createPost() {
        return ResponseEntity.ok("Post created");
    }
    
    @PostMapping("/comments")
    @RateLimit(key = "X-User-Id", type = "second", limit = 10)
    public ResponseEntity<String> createComment() {
        return ResponseEntity.ok("Comment created");
    }
}
```

## Best Practices

1. **Header Selection**
   - Use appropriate headers for rate limit keys
   - Common choices: X-User-Id, X-IP-Address, X-Device-Id
   - Consider security implications

2. **Rate Limit Values**
   - Set reasonable limits based on use case
   - Consider user experience
   - Monitor and adjust as needed

3. **Error Messages**
   - Provide clear error messages
   - Include retry information
   - Consider internationalization

4. **Monitoring**
   - Monitor rate limit hits
   - Track user behavior
   - Adjust limits based on usage

## Troubleshooting

### Common Issues

1. **Rate Limit Not Working**
   - Check header presence
   - Verify Redis connection
   - Review configuration

2. **Performance Issues**
   - Monitor Redis operations
   - Check connection pool
   - Review logging

3. **Incorrect Limits**
   - Verify configuration
   - Check annotation values
   - Review Redis data 