package com.mlspamdetection.webapp_backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration class for API rate limiting.
 * 
 * <p>This class configures rate limiting for the application using the Bucket4j library,
 * which implements the token bucket algorithm. Rate limiting is an important security measure
 * that helps protect the application from abuse, denial of service attacks, and ensures
 * fair usage of resources.</p>
 * 
 * <p>The token bucket algorithm works by creating a "bucket" that holds a certain number of tokens.
 * Each API request consumes one token. If the bucket is empty, further requests are rejected
 * until the bucket is refilled according to a defined rate.</p>
 * 
 * <p>In this implementation, rate limiting is applied to authentication endpoints to prevent
 * brute force attacks. Each client (identified by IP address) gets their own bucket.</p>
 */
@Configuration
public class RateLimitingConfig {

    /**
     * Creates a concurrent map to store rate limiting buckets.
     * 
     * <p>This bean maintains a mapping between client identifiers (typically IP addresses)
     * and their corresponding rate limit buckets. Using a ConcurrentHashMap ensures thread-safety
     * for concurrent access in a multi-threaded web application environment.</p>
     * 
     * @return A thread-safe map for storing rate limiting buckets
     */
    @Bean
    public Map<String, Bucket> buckets() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Creates a new rate limiting bucket with the specified limits.
     * 
     * <p>This method configures a bucket with a limit of 10 requests per minute as specified
     * in the application requirements. The bucket uses a "greedy" refill strategy, which means
     * all tokens are added at once at the beginning of each refill interval.</p>
     * 
     * <p>This rate limit helps prevent brute force attacks on authentication endpoints while
     * still allowing legitimate users to make a reasonable number of requests.</p>
     * 
     * @return A configured Bucket4j bucket instance with the specified rate limits
     */
    public Bucket createNewBucket() {
        // 10 requests per minute as specified in requirements
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
