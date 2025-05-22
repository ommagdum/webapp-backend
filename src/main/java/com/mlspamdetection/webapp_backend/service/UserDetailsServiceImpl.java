package com.mlspamdetection.webapp_backend.service;

import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.repo.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementation of Spring Security's UserDetailsService interface for authentication.
 * 
 * <p>This service is responsible for loading user-specific data during the authentication process.
 * It retrieves user information from the database and converts it into a format that Spring Security
 * can understand and use for authentication and authorization purposes.</p>
 * 
 * <p>Key responsibilities include:</p>
 * <ul>
 *   <li>Retrieving user data based on the provided email (username)</li>
 *   <li>Verifying that non-Google users have verified their email addresses</li>
 *   <li>Converting application-specific User entities to Spring Security's UserDetails objects</li>
 *   <li>Mapping user roles to Spring Security authorities</li>
 * </ul>
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    /**
     * Repository for accessing user data.
     */
    private final UserRepository userRepository;

    /**
     * Constructs a UserDetailsServiceImpl with the necessary dependencies.
     *
     * @param userRepository repository for user data access
     */
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by their email address (used as the username in this application).
     * 
     * <p>This method is called by Spring Security during the authentication process to retrieve
     * user details based on the provided email. It performs the following operations:</p>
     * <ol>
     *   <li>Retrieves the user from the database using the email</li>
     *   <li>Verifies that non-Google users have confirmed their email addresses</li>
     *   <li>Converts the application User entity to Spring Security's UserDetails</li>
     *   <li>Sets up the user's authorities based on their role</li>
     * </ol>
     *
     * @param email the email address (username) to load the user by
     * @return a UserDetails object containing the user's authentication information
     * @throws UsernameNotFoundException if the user is not found or their email is not verified
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!"google".equals(user.getAuthProvider()) && !user.isVerified()) {
            throw new UsernameNotFoundException("User email not verified");
        }

        // Fetch roles dynamically
        Collection<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities) // Set authorities dynamically
                .build();
    }


}

