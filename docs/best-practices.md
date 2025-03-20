# Rate Limiter Best Practices

This document outlines best practices for implementing and using the Rate Limiter service.

## Implementation Best Practices

### 1. Header Selection

- **Use Appropriate Headers**
  ```java
  // Good
  @RateLimit(key = "X-User-Id", type = "second", limit = 5)
  
  // Bad
  @RateLimit(key = "user-id", type = "second", limit = 5)  // Missing X- prefix
  ```

- **Common Header Choices**
  - `X-User-Id`: For user-specific limits
  - `X-IP-Address`: For IP-based limits
  - `X-Device-Id`: For device-specific limits
  - `X-API-Key`: For API key-based limits

### 2. Rate Limit Values

- **Set Reasonable Limits**
  ```java
  // Good
  @RateLimit(key = "X-User-Id", type = "second", limit = 5)  // 5 requests per second
  
  // Bad
  @RateLimit(key = "X-User-Id", type = "second", limit = 1000)  // Too high
  ```

- **Consider Use Cases**
  - API endpoints: Lower limits
  - Internal services: Higher limits
  - Public endpoints: Stricter limits

### 3. Time Windows

- **Choose Appropriate Windows**
  ```java
  // Good
  @RateLimit(key = "X-User-Id", type = "second", limit = 5)  // Short window
  @RateLimit(key = "X-IP-Address", type = "day", limit = 100)  // Long window
  
  // Bad
  @RateLimit(key = "X-User-Id", type = "week", limit = 1000)  // Too long window
  ```

- **Combine Windows**
  ```java
  @PostMapping("/resource")
  @RateLimit(key = "X-User-Id", type = "second", limit = 5)
  @RateLimit(key = "X-IP-Address", type = "day", limit = 100)
  public ResponseEntity<String> yourEndpoint() {
      return ResponseEntity.ok("Success");
  }
  ```

## Configuration Best Practices

### 1. Redis Configuration

```yaml
spring:
  data:
    redis:
      host: redis.example.com
      port: 6379
      password: ${REDIS_PASSWORD}
      timeout: 2000
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1
```

### 2. Rate Limiter Configuration

```yaml
rate-limiter:
  # Default limits
  max-requests-per-second: 2
  max-requests-per-day: 10
  max-requests-per-week: 5
  
  # Redis configuration
  key-prefix: "rate-limit:"
  
  # Monitoring
  logging:
    enabled: true
    level: INFO
```

## Security Best Practices

### 1. Header Security

- **Use Secure Headers**
  ```java
  // Good
  @RateLimit(key = "X-API-Key", type = "second", limit = 5)
  
  // Bad
  @RateLimit(key = "api-key", type = "second", limit = 5)  // Missing X- prefix
  ```

- **Validate Headers**
  ```java
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body("Invalid header: " + e.getMessage());
  }
  ```

### 2. Rate Limit Bypass Prevention

- **Server-Side Validation**
  ```java
  @Aspect
  @Component
  public class RateLimitAspect {
      public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
          // Validate headers
          validateHeaders(rateLimit);
          // Check rate limit
          return checkLimit(rateLimit);
      }
  }
  ```

- **Distributed Storage**
  ```java
  @Service
  public class RateLimiterService {
      private final RedisTemplate<String, String> redisTemplate;
      
      public boolean isAllowed(String key, String type, int limit) {
          // Use Redis for distributed storage
          return checkRedisLimit(key, type, limit);
      }
  }
  ```

## Performance Best Practices

### 1. Redis Operations

- **Efficient Key Structure**
  ```java
  // Good
  String key = String.format("%s:%s", prefix, identifier);
  
  // Bad
  String key = prefix + ":" + identifier;  // Less efficient
  ```

- **Atomic Operations**
  ```java
  @Service
  public class RateLimiterService {
      public boolean isAllowed(String key, String type, int limit) {
          // Use atomic operations
          return redisTemplate.opsForValue()
              .increment(key) <= limit;
      }
  }
  ```

### 2. Caching

- **Use Local Cache**
  ```java
  @Service
  public class RateLimiterService {
      private final Cache<String, Integer> localCache;
      
      public boolean isAllowed(String key, String type, int limit) {
          // Check local cache first
          Integer count = localCache.get(key);
          if (count != null && count >= limit) {
              return false;
          }
          // Check Redis
          return checkRedisLimit(key, type, limit);
      }
  }
  ```

## Monitoring Best Practices

### 1. Logging

```java
@Aspect
@Component
@Slf4j
public class RateLimitAspect {
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        try {
            // Check rate limit
            boolean allowed = rateLimiterService.isAllowed(key, type, limit);
            log.info("Rate limit check: key={}, type={}, allowed={}", key, type, allowed);
            return allowed;
        } catch (Exception e) {
            log.error("Rate limit error: key={}, type={}", key, type, e);
            throw e;
        }
    }
}
```

### 2. Metrics

```java
@Service
public class RateLimiterService {
    private final MeterRegistry registry;
    
    public boolean isAllowed(String key, String type, int limit) {
        boolean allowed = checkLimit(key, type, limit);
        registry.counter("rate.limit.checks", 
            "key", key,
            "type", type,
            "allowed", String.valueOf(allowed)
        ).increment();
        return allowed;
    }
}
```

## Deployment Best Practices

### 1. Environment Configuration

```yaml
# application-prod.yml
rate-limiter:
  max-requests-per-second: 5
  max-requests-per-day: 100
  max-requests-per-week: 50

# application-staging.yml
rate-limiter:
  max-requests-per-second: 10
  max-requests-per-day: 200
  max-requests-per-week: 100

# application-dev.yml
rate-limiter:
  max-requests-per-second: 20
  max-requests-per-day: 500
  max-requests-per-week: 200
```

### 2. Scaling

- **Use Redis Cluster**
  ```yaml
  spring:
    data:
      redis:
        cluster:
          nodes:
            - redis-1:6379
            - redis-2:6379
            - redis-3:6379
  ```

- **Load Balancing**
  ```yaml
  spring:
    cloud:
      loadbalancer:
        ribbon:
          enabled: true
  ```

## Maintenance Best Practices

### 1. Regular Updates

- Keep dependencies updated
- Monitor Redis performance
- Review rate limit values
- Update documentation

### 2. Monitoring

- Monitor rate limit hits
- Track Redis operations
- Watch for errors
- Review logs regularly

### 3. Backup

- Backup Redis data
- Document configurations
- Maintain test data
- Version control 