package com.mlspamdetection.webapp_backend.service;

import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.repo.UserRepository;
import com.mlspamdetection.webapp_backend.util.GoogleUserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

