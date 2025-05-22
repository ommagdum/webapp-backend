package com.mlspamdetection.webapp_backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Service responsible for sending emails to users.
 * 
 * <p>This service provides functionality for sending various types of emails
 * to users, such as verification emails for account activation. It uses Spring's
 * JavaMailSender to compose and send HTML-formatted emails.</p>
 * 
 * <p>The service is configured with the application's base URL to construct
 * proper verification links that users can click to verify their email addresses.</p>
 */
@Service
public class EmailService {

    /**
     * Spring's mail sender component for sending emails.
     */
    @Autowired
    private JavaMailSender mailSender;

    /**
     * Base URL of the application, used for constructing verification links.
     * Injected from application properties.
     */
    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Sends a verification email to a user with a verification token.
     * 
     * <p>This method composes and sends an HTML-formatted email containing a verification
     * link that the user can click to verify their email address. The verification link
     * includes a token that is used to identify and verify the user's account.</p>
     * 
     * <p>The email includes:</p>
     * <ul>
     *   <li>A thank you message for signing up</li>
     *   <li>A clickable verification link with the provided token</li>
     * </ul>
     *
     * @param to the recipient's email address
     * @param token the verification token to include in the verification link
     * @throws MessagingException if there is an error creating or sending the email
     */
    public void sendVerificationEmail(String to, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setFrom("verification.mlspamdetect@gmail.com");
        helper.setSubject("Email Verification - ML Spam Detection");
        String verificationLink = baseUrl + "/api/auth/verify?token=" + token;
        String emailContent = "<p> Thank you for signing up for ML Spam Detection. Please click the link below to verify your email address:</p>" +
                "<p><a href=\"" + verificationLink + "\">Verify Email</a></p>";
        helper.setText(emailContent, true);
        mailSender.send(message);
    }
}
