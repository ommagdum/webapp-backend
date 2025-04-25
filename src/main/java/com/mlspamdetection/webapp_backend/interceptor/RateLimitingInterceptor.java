package com.mlspamdetection.webapp_backend.interceptor;

import com.mlspamdetection.webapp_backend.config.RateLimitingConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> buckets;
    private final RateLimitingConfig rateLimitingConfig;

    @Autowired
    public RateLimitingInterceptor(Map<String, Bucket> buckets, RateLimitingConfig rateLimitingConfig) {
        this.buckets = buckets;
        this.rateLimitingConfig = rateLimitingConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ipAddress = getClientIP(request);

        // Get the bucket for this IP, create a new one if it doesn't exist
        Bucket bucket = buckets.computeIfAbsent(ipAddress, k -> rateLimitingConfig.createNewBucket());

        // Try to consume a token
        if (bucket.tryConsume(1)) {
            return true;
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests - please try again later");
            return false;
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
