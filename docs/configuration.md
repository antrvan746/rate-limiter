# Rate Limiter Configuration

This document describes all configuration options available in the Rate Limiter service.

## Application Properties

### Redis Configuration

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your-password  # Optional
      database: 0
      timeout: 2000
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1
```

### Rate Limiter Configuration

```yaml
rate-limiter:
  # Default rate limits
  max-requests-per-second: 2
  max-requests-per-day: 10
  max-requests-per-week: 5
  
  # Redis key prefix (optional)
  key-prefix: "rate-limit:"
  
  # Enable/disable rate limiting (default: true)
  enabled: true
  
  # Logging configuration
  logging:
    enabled: true
    level: INFO
```

## Annotation Configuration

### Rate Limit Annotation

```java
@RateLimit(
    key = "X-User-Id",      // Required: Header name for rate limit key
    type = "second",        // Required: Time window type
    limit = 5              // Optional: Custom limit (0 for default)
)
```

### Supported Time Windows

1. **Second-based**
   ```java
   @RateLimit(type = "second", limit = 5)
   ```

2. **Day-based**
   ```java
   @RateLimit(type = "day", limit = 10)
   ```

3. **Week-based**
   ```java
   @RateLimit(type = "week", limit = 1)
   ```

## Java Configuration

### Enable Rate Limiting

```java
@Configuration
@EnableRateLimiting
public class RateLimiterConfig {
    // Configuration beans
}
```

### Custom Redis Configuration

```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
```

## Environment Variables

You can override configuration using environment variables:

```bash
export SPRING_DATA_REDIS_HOST=redis.example.com
export SPRING_DATA_REDIS_PORT=6379
export RATE_LIMITER_MAX_REQUESTS_PER_SECOND=10
```

## Configuration Precedence

1. Annotation values (highest priority)
2. Environment variables
3. Application properties
4. Default values (lowest priority)

## Validation Rules

1. **Rate Limit Values**
   - Must be positive integers
   - Zero means use default value
   - Maximum value: Integer.MAX_VALUE

2. **Time Windows**
   - Must be one of: "second", "day", "week"
   - Case-insensitive

3. **Header Keys**
   - Must be non-empty strings
   - Should follow HTTP header naming conventions

## Logging Configuration

```yaml
logging:
  level:
    com.example.ratelimiter: INFO
    org.springframework.data.redis: WARN
```

## Monitoring Configuration

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,rate-limits
  metrics:
    tags:
      application: ${spring.application.name}
```

## Security Configuration

```yaml
spring:
  security:
    headers:
      frame-options: DENY
      xss-protection: 1; mode=block
      content-security-policy: default-src 'self'
```

## Troubleshooting

### Common Issues

1. **Redis Connection**
   - Check Redis server is running
   - Verify connection credentials
   - Check network connectivity

2. **Rate Limit Not Working**
   - Verify annotation is present
   - Check header is being sent
   - Validate Redis operations

3. **Performance Issues**
   - Monitor Redis connection pool
   - Check for Redis key expiration
   - Review logging levels 