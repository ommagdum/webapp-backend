package com.mlspamdetection.webapp_backend.service;

import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.repo.UserRepository;
import com.mlspamdetection.webapp_backend.security.JwtUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for handling email verification processes.
 * 
 * <p>This service manages the email verification workflow for new user registrations,
 * including generating verification tokens, sending verification emails, and processing
 * verification requests when users click on verification links.</p>
 * 
 * <p>The service works closely with the EmailService to send the actual verification
 * emails and with the UserRepository to update user verification status.</p>
 */
@Service
public class EmailVerificationService {

    /**
     * Repository for accessing and updating user data.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Service for sending emails to users.
     */
    @Autowired
    private EmailService emailService;

    /**
     * Utility for JWT token operations.
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Sends a verification email to a newly registered user.
     * 
     * <p>This method generates a unique verification token using UUID, associates it with
     * the user account, and sends an email containing a verification link with this token.</p>
     * 
     * <p>The verification token is stored in the user's record in the database and will be
     * used to verify the user's email address when they click the verification link.</p>
     *
     * @param user the user who needs email verification
     * @throws MessagingException if there is an error sending the email
     */
    public void sendVerificationEmail(User user) throws MessagingException {
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    /**
     * Verifies a user's email address using the provided verification token.
     * 
     * <p>This method is called when a user clicks on the verification link in their email.
     * It looks up the user by the verification token, and if found, marks the user as verified
     * and clears the verification token.</p>
     * 
     * <p>The verification process follows these steps:</p>
     * <ol>
     *   <li>Find the user with the matching verification token</li>
     *   <li>If found, mark the user as verified</li>
     *   <li>Clear the verification token (it's single-use)</li>
     *   <li>Save the updated user information</li>
     * </ol>
     *
     * @param token the verification token from the email link
     * @return true if verification was successful, false if the token was invalid or not found
     */
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

    /**
     * Resends a verification email to a user who hasn't verified their account yet.
     * 
     * <p>This method allows users to request a new verification email if they didn't
     * receive the original one or if the original verification link expired. It checks
     * if the user exists and is not already verified before sending a new verification email.</p>
     * 
     * <p>The method follows these steps:</p>
     * <ol>
     *   <li>Find the user by email address</li>
     *   <li>Check if the user exists and is not already verified</li>
     *   <li>If both conditions are met, generate a new verification token and send a new email</li>
     * </ol>
     *
     * @param email the email address of the user requesting a new verification email
     * @return true if a new verification email was sent, false if the user doesn't exist or is already verified
     * @throws MessagingException if there is an error sending the email
     */
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
