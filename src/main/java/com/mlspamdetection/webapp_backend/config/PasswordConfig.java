package com.mlspamdetection.webapp_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class for password encoding.
 * 
 * <p>This class provides a bean for password encoding using BCrypt, which is a strong
 * one-way hashing algorithm specifically designed for password hashing.</p>
 * 
 * <p>BCrypt automatically handles salting of passwords and has a configurable work factor
 * that allows the algorithm to be slowed down as hardware gets faster, maintaining its
 * resistance to brute-force attacks over time.</p>
 * 
 * <p>The PasswordEncoder bean is used throughout the application for:</p>
 * <ul>
 *   <li>Encoding passwords during user registration</li>
 *   <li>Verifying passwords during authentication</li>
 *   <li>Encoding passwords during password reset operations</li>
 * </ul>
 */
@Configuration
public class PasswordConfig {

    /**
     * Creates a BCryptPasswordEncoder bean for secure password hashing.
     * 
     * <p>This implementation uses the BCrypt strong hashing function with a default strength of 10.
     * The higher the strength parameter, the more work is required to compute the hash, making
     * brute force attacks more difficult but also increasing the time required to hash passwords.</p>
     * 
     * @return A configured PasswordEncoder instance using BCrypt algorithm
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
