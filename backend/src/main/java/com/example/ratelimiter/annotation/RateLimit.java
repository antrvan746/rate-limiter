package com.example.ratelimiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    String key() default ""; // The header name to use as the rate limit key

    String type() default "second"; // The rate limit type (second/day/week)

    int limit() default 0; // The rate limit value
}