# Rate Limiter Service

A flexible and configurable rate limiting service built with Spring Boot that supports different rate limit types and custom limits per endpoint.

## Features

- Configurable rate limits per endpoint
- Support for different time windows (second, day, week)
- Custom rate limit values per endpoint
- Header-based rate limit keys
- Redis-based rate limit storage
- Aspect-oriented rate limit enforcement
- Comprehensive test coverage

## Architecture

The service is built using the following components:

### 1. Rate Limit Annotation
```java
@RateLimit(key = "X-User-Id", type = "second", limit = 5)
```
- `key`: The HTTP header to use as the rate limit key
- `type`: The time window type (second/day/week)
- `limit`: The maximum number of requests allowed in the time window

### 2. Core Components

- **RateLimitAspect**: Handles rate limit checking using AOP
- **RateLimiterService**: Core service implementing rate limiting logic
- **RateLimiterConfig**: Configuration for default rate limits
- **RedisTemplate**: Used for distributed rate limit storage

## Usage

### 1. Add Rate Limit to Endpoints

```java
@RestController
@RequestMapping("/api")
public class YourController {
    
    @PostMapping("/resource")
    @RateLimit(key = "X-User-Id", type = "second", limit = 10)
    public ResponseEntity<String> yourEndpoint() {
        // Your endpoint logic
    }
}
```

### 2. Configure Rate Limits

In `application.yml`:
```yaml
rate-limiter:
  max-requests-per-second: 2
  max-requests-per-day: 10
  max-requests-per-week: 5
```

### 3. Required Headers

When making requests to rate-limited endpoints, include the appropriate header:
```bash
curl -X POST http://your-api/resource \
  -H "X-User-Id: user123"
```

## Rate Limit Types

1. **Second-based Rate Limit**
   - Resets every second
   - Example: 5 requests per second

2. **Day-based Rate Limit**
   - Resets every 24 hours
   - Example: 10 requests per day

3. **Week-based Rate Limit**
   - Resets every 7 days
   - Example: 1 request per week

## Response Codes

- `200 OK`: Request allowed
- `429 Too Many Requests`: Rate limit exceeded
- `400 Bad Request`: Missing required header

## Testing

The service includes comprehensive test coverage:

### Unit Tests
- `RateLimitAspectTest`: Tests the aspect's behavior
- `RateLimiterServiceTest`: Tests core rate limiting logic

### Integration Tests
- `RateLimiterIntegrationTest`: Tests complete HTTP request/response flow

Run tests:
```bash
mvn test
```

## Dependencies

- Spring Boot
- Spring AOP
- Spring Data Redis
- Redis
- JUnit 5
- Mockito

## Error Handling

The service includes a global exception handler for:
- Rate limit exceeded scenarios
- Missing headers
- Invalid rate limit types

## Best Practices

1. **Header Selection**
   - Use appropriate headers for rate limit keys
   - Common choices: X-User-Id, X-IP-Address, X-Device-Id

2. **Rate Limit Values**
   - Set reasonable limits based on your use case
   - Consider using different limits for different endpoints

3. **Time Windows**
   - Choose appropriate time windows for your use case
   - Consider combining different time windows for stricter control

## Example Endpoints

```java
@RestController
@RequestMapping("/api")
public class RateLimiterController {

    @PostMapping("/posts")
    @RateLimit(key = "X-User-Id", type = "second", limit = 5)
    public ResponseEntity<String> createPost() {
        return ResponseEntity.ok("Post created successfully");
    }

    @PostMapping("/accounts")
    @RateLimit(key = "X-IP-Address", type = "day", limit = 3)
    public ResponseEntity<String> createAccount() {
        return ResponseEntity.ok("Account created successfully");
    }

    @PostMapping("/rewards")
    @RateLimit(key = "X-Device-Id", type = "week", limit = 1)
    public ResponseEntity<String> claimReward() {
        return ResponseEntity.ok("Reward claimed successfully");
    }
}
```

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 