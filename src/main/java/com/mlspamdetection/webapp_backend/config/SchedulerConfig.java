package com.mlspamdetection.webapp_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for scheduling-related components.
 * 
 * <p>This class enables Spring's scheduling capabilities through the {@code @EnableScheduling} annotation,
 * allowing the application to execute scheduled tasks defined with {@code @Scheduled} annotations.</p>
 * 
 * <p>It also provides a {@link RestTemplate} bean that can be used throughout the application
 * for making HTTP requests to external services, particularly useful for scheduled tasks
 * that need to communicate with external APIs.</p>
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    /**
     * Creates and configures a RestTemplate bean.
     * 
     * <p>This bean is used for making HTTP requests to external services and APIs.
     * It uses the default configuration without any custom request factories or interceptors.</p>
     * 
     * @return A configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
