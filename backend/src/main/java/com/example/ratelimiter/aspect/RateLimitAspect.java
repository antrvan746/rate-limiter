package com.example.ratelimiter.aspect;

import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.ratelimiter.annotation.RateLimit;
import com.example.ratelimiter.exception.RateLimitExceededException;
import com.example.ratelimiter.service.RateLimiterService;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    private final RateLimiterService rateLimiterService;

    @Around("@annotation(rateLimit)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        String key = request.getHeader(rateLimit.key());

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Required header " + rateLimit.key() + " is missing");
        }

        if (!rateLimiterService.isAllowed(key, rateLimit.type(), rateLimit.limit())) {
            throw new RateLimitExceededException("Rate limit exceeded for " + rateLimit.type());
        }

        return joinPoint.proceed();
    }
}