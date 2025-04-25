package com.mlspamdetection.webapp_backend.service;

import com.mlspamdetection.webapp_backend.dto.TokenRefreshRequest;
import com.mlspamdetection.webapp_backend.dto.TokenRefreshResponse;
import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Autowired
    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

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
