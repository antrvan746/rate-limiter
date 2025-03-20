# Getting Started with Rate Limiter

This guide will help you get started with the Rate Limiter service.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Redis server
- Spring Boot 3.x

## Installation

1. Add the rate limiter dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>rate-limiter</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. Configure Redis in your `application.yml`:

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

3. Enable rate limiting in your Spring Boot application:

```java
@SpringBootApplication
@EnableRateLimiting
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

## Basic Usage

1. Add the `@RateLimit` annotation to your endpoint:

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

2. Make requests with the required header:

```bash
curl -X POST http://your-api/resource \
  -H "X-User-Id: user123"
```

## Next Steps

- Read the [Architecture](architecture.md) guide to understand how the rate limiter works
- Check out the [Configuration](configuration.md) guide for detailed configuration options
- See [Examples](examples.md) for more usage scenarios
- Review [Best Practices](best-practices.md) for production deployment 