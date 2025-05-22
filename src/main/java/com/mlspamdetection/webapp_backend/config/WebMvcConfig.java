package com.mlspamdetection.webapp_backend.config;

import com.mlspamdetection.webapp_backend.interceptor.RateLimitingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for Spring MVC customization.
 * 
 * <p>This class implements the WebMvcConfigurer interface, which provides callback methods
 * to customize the Java-based configuration for Spring MVC. It allows the application to
 * customize various aspects of the Spring MVC framework without having to declare a complete
 * configuration.</p>
 * 
 * <p>In this implementation, the primary focus is on registering interceptors that process
 * HTTP requests before they reach the controllers. Specifically, it configures rate limiting
 * for authentication-related endpoints to prevent brute force attacks.</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * The rate limiting interceptor that will be applied to specific endpoints.
     */
    private final RateLimitingInterceptor rateLimitingInterceptor;

    /**
     * Constructs a new WebMvcConfig with the specified rate limiting interceptor.
     * 
     * @param rateLimitingInterceptor The interceptor that implements rate limiting functionality
     */
    @Autowired
    public WebMvcConfig(RateLimitingInterceptor rateLimitingInterceptor) {
        this.rateLimitingInterceptor = rateLimitingInterceptor;
    }

    /**
     * Registers interceptors that process HTTP requests.
     * 
     * <p>This method adds the rate limiting interceptor to the interceptor registry and
     * configures it to only apply to specific authentication endpoints. These endpoints
     * are particularly sensitive to brute force attacks, so rate limiting is an important
     * security measure.</p>
     * 
     * <p>The following endpoints are rate-limited:</p>
     * <ul>
     *   <li>/api/login - User login endpoint</li>
     *   <li>/api/register - User registration endpoint</li>
     *   <li>/api/refresh-token - JWT token refresh endpoint</li>
     * </ul>
     * 
     * @param registry The interceptor registry to which interceptors are added
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Apply rate limiting only to authentication endpoints
        registry.addInterceptor(rateLimitingInterceptor)
                .addPathPatterns("/api/login", "/api/register", "/api/refresh-token");
    }
}
