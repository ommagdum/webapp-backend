package com.mlspamdetection.webapp_backend.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Configuration class for Cross-Origin Resource Sharing (CORS) settings.
 * 
 * <p>CORS is a security feature implemented by browsers that restricts web pages from making
 * requests to a different domain than the one that served the original page. This configuration
 * enables controlled cross-origin requests necessary for the frontend to communicate with this API.</p>
 * 
 * <p>This configuration is particularly important for separating the frontend (running on a different domain/port)
 * from the backend API while allowing them to communicate securely.</p>
 */
@Configuration
public class CorsConfig {

    /**
     * Creates and configures a CORS filter bean.
     * 
     * <p>This filter allows cross-origin requests from the specified frontend origin (localhost:5173)
     * and configures various CORS settings such as allowed methods, headers, and credentials.</p>
     * 
     * <p>The filter exposes specific headers that the frontend may need to access:</p>
     * <ul>
     *   <li>Authorization - For JWT token-based authentication</li>
     *   <li>X-Admin-Secret - For admin-specific operations</li>
     *   <li>X-Total-Count - For pagination information</li>
     * </ul>
     * 
     * @return A configured CorsFilter instance
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setExposedHeaders(List.of("X-Admin-Secret"));
        config.setExposedHeaders(List.of("X-Total-Count"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // 1 hour cache for preflight requests

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}