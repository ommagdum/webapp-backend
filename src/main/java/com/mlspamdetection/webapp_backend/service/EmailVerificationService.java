package com.mlspamdetection.webapp_backend.service;

import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.repo.UserRepository;
import com.mlspamdetection.webapp_backend.security.JwtUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class EmailVerificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    public void sendVerificationEmail(User user) throws MessagingException {
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setVerified(true);
            user.setVerificationToken(null);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    public boolean resendVerificationEmail(String email) throws MessagingException {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.isVerified()) {
                sendVerificationEmail(user);
                return true;
            }
        }
        return false;
    }


}
