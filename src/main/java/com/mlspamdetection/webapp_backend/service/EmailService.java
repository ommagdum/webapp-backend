package com.mlspamdetection.webapp_backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

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
