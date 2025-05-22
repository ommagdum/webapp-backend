package com.mlspamdetection.webapp_backend.repo;

import com.mlspamdetection.webapp_backend.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 * 
 * <p>This repository provides CRUD operations for the User entity and additional
 * query methods for finding users by various attributes. It extends Spring Data JPA's
 * {@link JpaRepository} to leverage built-in data access functionality.</p>
 * 
 * <p>The repository is used for user authentication, profile management, and user-related
 * operations throughout the application.</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds a user by their email address.
     * 
     * <p>This method is primarily used for authentication and account management.</p>
     * 
     * @param email the email address to search for
     * @return an Optional containing the user if found, or empty if no user exists with the given email
     */
    Optional<User> findByEmail(String email);
    /**
     * Finds a user by their JWT refresh token.
     * 
     * <p>This method is used during the token refresh process to identify the user
     * requesting a new access token.</p>
     * 
     * @param refreshToken the refresh token to search for
     * @return an Optional containing the user if found, or empty if no user exists with the given token
     */
    Optional<User> findByRefreshToken(String refreshToken);

    /**
     * Finds a user by their email verification token.
     * 
     * <p>This method is used during the account verification process to identify the user
     * who is verifying their email address.</p>
     * 
     * @param token the verification token to search for
     * @return an Optional containing the user if found, or empty if no user exists with the given token
     */
    Optional<User> findByVerificationToken(String token);

    /**
     * Checks if a user with the given email address exists.
     * 
     * <p>This method is used during registration to prevent duplicate accounts
     * with the same email address.</p>
     * 
     * @param email the email address to check
     * @return true if a user with the given email exists, false otherwise
     */
    boolean existsByEmail(String email);
    /**
     * Finds a user by their Google OAuth ID.
     * 
     * <p>This method is used during OAuth2 authentication with Google to identify
     * returning users who have previously authenticated with Google.</p>
     * 
     * @param googleId the Google ID to search for
     * @return an Optional containing the user if found, or empty if no user exists with the given Google ID
     */
    Optional<User> findByGoogleId(String googleId);
    /**
     * Retrieves a paginated list of all users.
     * 
     * <p>This method is typically used in admin interfaces to display and manage users.</p>
     * 
     * @param pageable pagination information including page number, page size, and sorting
     * @return a Page of User entities
     */
    Page<User> findAll(Pageable pageable);
}
