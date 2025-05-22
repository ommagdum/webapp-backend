package com.mlspamdetection.webapp_backend.service;

import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.repo.UserRepository;
import com.mlspamdetection.webapp_backend.util.GoogleUserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for user management operations.
 * 
 * <p>This service provides functionality for managing user accounts, including
 * creating new users, finding existing users, and handling OAuth authentication
 * with Google. It works with the {@link UserRepository} to persist user data.</p>
 * 
 * <p>The service supports both traditional email/password authentication and
 * OAuth-based authentication with Google, ensuring that users can seamlessly
 * use either authentication method.</p>
 */
@Service
public class UserService {

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Encoder for securely hashing user passwords.
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Finds an existing user by email or creates a new user with Google OAuth data.
     * 
     * <p>This method is used during Google OAuth authentication to either retrieve an
     * existing user account or create a new one based on the Google user data. If an
     * existing user is found with the same email but no Google ID, the method updates
     * the user's record to link it with their Google account.</p>
     * 
     * <p>For new users, the method:</p>
     * <ul>
     *   <li>Creates a new user record with the Google email</li>
     *   <li>Sets the Google ID for future authentication</li>
     *   <li>Sets the authentication provider to "google"</li>
     *   <li>Generates a random secure password (not used for login but required by the data model)</li>
     *   <li>Marks the account as verified (since Google has already verified the email)</li>
     * </ul>
     *
     * @param userData the Google user data containing email and Google ID
     * @return the existing or newly created user entity
     */
    public User findOrCreateGoogleUser(GoogleUserData userData) {
        Optional<User> existingUser = userRepository.findByEmail(userData.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update Google ID if not set
            if (user.getGoogleId() == null) {
                user.setGoogleId(userData.getGoogleId());
                user.setAuthProvider("google");
                return userRepository.save(user);
            }
            return user;
        } else {
            // Create new user
            User newUser = new User();
            newUser.setEmail(userData.getEmail());
            newUser.setGoogleId(userData.getGoogleId());
            newUser.setAuthProvider("google");
            // Generate a random password for Google users
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setVerified(true); // Google users are pre-verified

            return userRepository.save(newUser);
        }
    }
}

