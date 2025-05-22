package com.mlspamdetection.webapp_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Configuration class for email functionality.
 * 
 * <p>This class configures the JavaMailSender bean which is used throughout the application
 * for sending emails. It reads mail server configuration from application properties and
 * sets up the necessary SMTP settings.</p>
 * 
 * <p>Email functionality is used for various purposes in the application including:</p>
 * <ul>
 *   <li>User registration confirmation</li>
 *   <li>Password reset requests</li>
 *   <li>Notifications about suspicious activity</li>
 *   <li>System alerts for administrators</li>
 * </ul>
 */
@Configuration
public class MailConfig {

    /**
     * SMTP server host name injected from application properties.
     */
    @Value("${spring.mail.host}")
    private String host;

    /**
     * SMTP server port number injected from application properties.
     */
    @Value("${spring.mail.port}")
    private int port;

    /**
     * Email account username injected from application properties.
     */
    @Value("${spring.mail.username}")
    private String username;

    /**
     * Email account password injected from application properties.
     */
    @Value("${spring.mail.password}")
    private String password;

    /**
     * Creates and configures a JavaMailSender bean for sending emails.
     * 
     * <p>This bean is configured with the SMTP server details and authentication credentials
     * from the application properties. It also sets up various mail properties including:</p>
     * <ul>
     *   <li>SMTP as the transport protocol</li>
     *   <li>SMTP authentication</li>
     *   <li>STARTTLS for secure communication</li>
     *   <li>Debug mode (should be disabled in production)</li>
     * </ul>
     * 
     * @return A configured JavaMailSender instance
     */
    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true"); // Set to false in production

        return mailSender;
    }
}
