package com.mlspamdetection.webapp_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Configuration class for bean validation.
 * 
 * <p>This class configures the validation framework used throughout the application for
 * validating input data. It provides a {@link LocalValidatorFactoryBean} which is Spring's
 * implementation of the Bean Validation API (JSR-380).</p>
 * 
 * <p>Bean validation is used to enforce constraints on model properties and method parameters,
 * ensuring that data meets specific criteria before being processed by the application. This
 * helps prevent invalid data from entering the system and improves overall data integrity.</p>
 * 
 * <p>Common validation annotations used with this validator include:</p>
 * <ul>
 *   <li>{@code @NotNull} - Validates that a value is not null</li>
 *   <li>{@code @NotEmpty} - Validates that a string, collection, map, or array is not null or empty</li>
 *   <li>{@code @Size} - Validates that a string, collection, map, or array size is between min and max</li>
 *   <li>{@code @Email} - Validates that a string is a valid email address</li>
 *   <li>{@code @Pattern} - Validates that a string matches a regular expression pattern</li>
 * </ul>
 */
@Configuration
public class ValidationConfig {

    /**
     * Creates and configures a LocalValidatorFactoryBean for bean validation.
     * 
     * <p>This bean is used to validate objects annotated with validation constraints.
     * It integrates with Spring's validation infrastructure and can be autowired into
     * controllers, services, or other components that need to perform validation.</p>
     * 
     * <p>The validator is typically used in conjunction with the {@code @Valid} or
     * {@code @Validated} annotations in controller methods to validate incoming request data.</p>
     * 
     * @return A configured LocalValidatorFactoryBean instance
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
}
