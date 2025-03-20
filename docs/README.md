# Rate Limiter Documentation

Welcome to the Rate Limiter documentation. This documentation will guide you through the implementation, configuration, and usage of the Rate Limiter service.

## Table of Contents

1. [Getting Started](getting-started.md)
2. [Architecture](architecture.md)
3. [Configuration](configuration.md)
4. [Usage Guide](usage-guide.md)
5. [API Reference](api-reference.md)
6. [Testing](testing.md)
7. [Best Practices](best-practices.md)
8. [Examples](examples.md)
9. [Contributing](contributing.md)

## Quick Start

1. Add the rate limiter dependency to your project
2. Configure Redis connection
3. Add rate limit annotations to your endpoints
4. Start using the rate limiter

For detailed instructions, see the [Getting Started](getting-started.md) guide.

This project implements a rate limiter using Spring Boot and Redis. It provides three different rate limiting scenarios:

1. Maximum 2 posts per second per user
2. Maximum 10 account creations per day per IP address
3. Maximum 5 reward claims per week per device

## Project Structure

```
rate-limiter/
├── backend/           # Spring Boot application
│   ├── src/          # Source code
│   ├── pom.xml       # Maven configuration
│   └── Dockerfile    # Docker configuration for the app
├── docs/             # Documentation
└── docker-compose.yml # Docker Compose configuration
```

## Prerequisites

- Docker
- Docker Compose

## Running the Application

1. Build and start the containers:
   ```bash
   docker-compose up --build
   ```

2. The application will be available at `http://localhost:8080`

## API Endpoints

### Create Post
- **URL**: `/api/posts`
- **Method**: `POST`
- **Header**: `X-User-Id: <user-id>`
- **Rate Limit**: 2 requests per second

### Create Account
- **URL**: `/api/accounts`
- **Method**: `POST`
- **Header**: `X-IP-Address: <ip-address>`
- **Rate Limit**: 10 requests per day

### Claim Reward
- **URL**: `/api/rewards`
- **Method**: `POST`
- **Header**: `X-Device-Id: <device-id>`
- **Rate Limit**: 5 requests per week

## Rate Limiting Configuration

Rate limits can be configured in `backend/src/main/resources/application.properties`:

```properties
rate-limiter.max-requests-per-second=2
rate-limiter.max-requests-per-day=10
rate-limiter.max-requests-per-week=5
```

## Development

To run the application locally without Docker:

1. Install Redis locally
2. Run Redis server
3. Build and run the Spring Boot application:
   ```bash
   cd backend
   mvn spring-boot:run
   ``` 