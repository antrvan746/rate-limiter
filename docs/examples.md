# Rate Limiter Examples

This document provides various examples of using the Rate Limiter service in different scenarios.

## Basic Examples

### 1. Simple Rate Limit

```java
@RestController
@RequestMapping("/api")
public class SimpleController {
    
    @PostMapping("/resource")
    @RateLimit(key = "X-User-Id", type = "second", limit = 5)
    public ResponseEntity<String> simpleEndpoint() {
        return ResponseEntity.ok("Success");
    }
}
```

### 2. Multiple Rate Limits

```java
@RestController
@RequestMapping("/api")
public class MultipleLimitsController {
    
    @PostMapping("/resource")
    @RateLimit(key = "X-User-Id", type = "second", limit = 5)
    @RateLimit(key = "X-IP-Address", type = "day", limit = 100)
    public ResponseEntity<String> multipleLimitsEndpoint() {
        return ResponseEntity.ok("Success");
    }
}
```

## API Rate Limiting Examples

### 1. Public API Rate Limiting

```java
@RestController
@RequestMapping("/api/public")
public class PublicApiController {
    
    @GetMapping("/products")
    @RateLimit(key = "X-IP-Address", type = "second", limit = 10)
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
    
    @GetMapping("/categories")
    @RateLimit(key = "X-IP-Address", type = "second", limit = 5)
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}
```

### 2. Private API Rate Limiting

```java
@RestController
@RequestMapping("/api/private")
public class PrivateApiController {
    
    @PostMapping("/orders")
    @RateLimit(key = "X-User-Id", type = "second", limit = 5)
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }
    
    @PutMapping("/orders/{id}")
    @RateLimit(key = "X-User-Id", type = "second", limit = 3)
    public ResponseEntity<Order> updateOrder(
            @PathVariable String id,
            @RequestBody OrderUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(id, request));
    }
}
```

## User Action Rate Limiting Examples

### 1. Login Attempts

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/login")
    @RateLimit(key = "X-IP-Address", type = "second", limit = 3)
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @PostMapping("/password-reset")
    @RateLimit(key = "X-IP-Address", type = "hour", limit = 1)
    public ResponseEntity<Void> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        authService.sendPasswordResetEmail(request.getEmail());
        return ResponseEntity.ok().build();
    }
}
```

### 2. User Registration

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @PostMapping("/register")
    @RateLimit(key = "X-IP-Address", type = "day", limit = 5)
    public ResponseEntity<User> register(@RequestBody RegistrationRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }
    
    @PostMapping("/verify-email")
    @RateLimit(key = "X-IP-Address", type = "hour", limit = 3)
    public ResponseEntity<Void> verifyEmail(@RequestBody EmailVerificationRequest request) {
        userService.verifyEmail(request.getToken());
        return ResponseEntity.ok().build();
    }
}
```

## Resource Creation Rate Limiting Examples

### 1. Content Creation

```java
@RestController
@RequestMapping("/api/content")
public class ContentController {
    
    @PostMapping("/posts")
    @RateLimit(key = "X-User-Id", type = "second", limit = 5)
    public ResponseEntity<Post> createPost(@RequestBody PostRequest request) {
        return ResponseEntity.ok(postService.createPost(request));
    }
    
    @PostMapping("/comments")
    @RateLimit(key = "X-User-Id", type = "second", limit = 10)
    public ResponseEntity<Comment> createComment(@RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.createComment(request));
    }
}
```

### 2. File Upload

```java
@RestController
@RequestMapping("/api/files")
public class FileController {
    
    @PostMapping("/upload")
    @RateLimit(key = "X-User-Id", type = "hour", limit = 10)
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(fileService.uploadFile(file));
    }
    
    @PostMapping("/batch-upload")
    @RateLimit(key = "X-User-Id", type = "hour", limit = 3)
    public ResponseEntity<List<FileUploadResponse>> batchUploadFiles(
            @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(fileService.batchUploadFiles(files));
    }
}
```

## Custom Rate Limit Key Examples

### 1. API Key Based

```java
@RestController
@RequestMapping("/api")
public class ApiKeyController {
    
    @GetMapping("/data")
    @RateLimit(key = "X-API-Key", type = "second", limit = 10)
    public ResponseEntity<Data> getData() {
        return ResponseEntity.ok(dataService.getData());
    }
}
```

### 2. Device Based

```java
@RestController
@RequestMapping("/api")
public class DeviceController {
    
    @PostMapping("/sync")
    @RateLimit(key = "X-Device-Id", type = "second", limit = 5)
    public ResponseEntity<SyncResponse> syncData(@RequestBody SyncRequest request) {
        return ResponseEntity.ok(syncService.syncData(request));
    }
}
```

## Error Handling Examples

### 1. Custom Error Response

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
            RateLimitExceededException e) {
        ErrorResponse response = new ErrorResponse(
            "RATE_LIMIT_EXCEEDED",
            e.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(response);
    }
}
```

### 2. Retry-After Header

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
            RateLimitExceededException e) {
        ErrorResponse response = new ErrorResponse(
            "RATE_LIMIT_EXCEEDED",
            e.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", e.getRetryAfterSeconds())
                .body(response);
    }
}
```

## Testing Examples

### 1. Unit Test

```java
@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {
    
    @Test
    void whenRateLimitAllowed_shouldProceed() throws Throwable {
        // Given
        when(rateLimit.key()).thenReturn("X-User-Id");
        when(rateLimit.type()).thenReturn("second");
        when(rateLimit.limit()).thenReturn(5);
        request.addHeader("X-User-Id", "user123");
        when(rateLimiterService.isAllowed(anyString(), anyString(), anyInt()))
            .thenReturn(true);
        
        // When
        Object result = aspect.checkRateLimit(mock(ProceedingJoinPoint.class), rateLimit);
        
        // Then
        assertNotNull(result);
        verify(rateLimiterService).isAllowed("user123", "second", 5);
    }
}
```

### 2. Integration Test

```java
@SpringBootTest
@AutoConfigureMockMvc
class RateLimiterIntegrationTest {
    
    @Test
    void whenRateLimitExceeded_shouldReturn429() throws Exception {
        // Make requests up to the limit
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/posts")
                    .header("X-User-Id", "user123"))
                    .andExpect(status().isOk());
        }
        
        // Next request should be rate limited
        mockMvc.perform(post("/api/posts")
                .header("X-User-Id", "user123"))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("Rate limit exceeded for second"));
    }
} 