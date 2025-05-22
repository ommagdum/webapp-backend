package com.mlspamdetection.webapp_backend.service;

import com.mlspamdetection.webapp_backend.dto.TokenRefreshRequest;
import com.mlspamdetection.webapp_backend.dto.TokenRefreshResponse;
import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service responsible for authentication-related operations.
 * 
 * <p>This service handles token refresh operations, validating refresh tokens,
 * and generating new access tokens. It works in conjunction with the JwtService
 * to manage JWT tokens for user authentication.</p>
 * 
 * <p>The service ensures that refresh tokens are valid, matches what's stored in the
 * database for the user, and generates new token pairs when needed.</p>
 */
@Service
public class AuthService {

    /**
     * Repository for accessing user data.
     */
    private final UserRepository userRepository;
    
    /**
     * Service for JWT token operations.
     */
    private final JwtService jwtService;

    /**
     * Constructs an AuthService with the necessary dependencies.
     *
     * @param userRepository repository for user data access
     * @param jwtService service for JWT token operations
     */
    @Autowired
    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    /**
     * Refreshes the authentication tokens for a user.
     * 
     * <p>This method validates the provided refresh token, ensures it matches what's
     * stored for the user, and generates a new pair of access and refresh tokens.</p>
     * 
     * <p>The method follows these steps:</p>
     * <ol>
     *   <li>Validates the refresh token's signature and expiration</li>
     *   <li>Extracts the username (email) from the token</li>
     *   <li>Finds the user associated with the email</li>
     *   <li>Verifies that the token matches what's stored for the user</li>
     *   <li>Generates new access and refresh tokens</li>
     *   <li>Updates the user's refresh token in the database</li>
     * </ol>
     *
     * <p>The method is transactional to ensure database consistency when updating
     * the user's refresh token.</p>
     *
     * @param request the token refresh request containing the refresh token
     * @return an Optional containing the new token pair if successful, or empty if the refresh failed
     */
    @Transactional
    public Optional<TokenRefreshResponse> refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        if (!jwtService.validateToken(refreshToken)) {
            return Optional.empty();
        }

        // Extract username from token
        String username = jwtService.extractUsername(refreshToken);

        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(username);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();

        // Verify that the token matches what's stored in the database
        if (!refreshToken.equals(user.getRefreshToken())) {
            return Optional.empty();
        }

        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        // Update refresh token in database
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return Optional.of(new TokenRefreshResponse(newAccessToken, newRefreshToken));
    }
}
