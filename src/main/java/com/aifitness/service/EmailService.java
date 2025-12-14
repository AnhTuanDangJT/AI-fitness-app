package com.aifitness.service;

import com.aifitness.exception.EmailServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Email Service
 * 
 * Handles sending emails for email verification.
 * All email credentials must come from environment variables.
 */
@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:}")
    private String mailUsername;
    
    @Value("${spring.mail.password:}")
    private String mailPassword;
    
    @Value("${app.email.from:}")
    private String fromEmail;
    
    private boolean isEmailConfigured = false;
    
    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    /**
     * Checks if email service is properly configured on startup.
     */
    @PostConstruct
    public void checkEmailConfiguration() {
        boolean hasUsername = mailUsername != null && !mailUsername.trim().isEmpty();
        boolean hasPassword = mailPassword != null && !mailPassword.trim().isEmpty();
        boolean hasFrom = fromEmail != null && !fromEmail.trim().isEmpty();
        
        isEmailConfigured = hasUsername && hasPassword && hasFrom;
        
        if (isEmailConfigured) {
            logger.info("Email service is configured - Host: {}, From: {}", 
                System.getenv("MAIL_HOST"), fromEmail);
        } else {
            logger.warn("Email service is NOT configured - Missing: username={}, password={}, from={}", 
                !hasUsername, !hasPassword, !hasFrom);
            logger.warn("Set MAIL_USERNAME, MAIL_PASSWORD, and APP_EMAIL_FROM environment variables");
        }
    }
    
    /**
     * Sends email verification code to user.
     * 
     * @param toEmail The recipient email address
     * @param verificationCode The 6-digit verification code
     * @throws EmailServiceException if email service is not configured or sending fails
     */
    public void sendVerificationEmail(String toEmail, String verificationCode) {
        if (!isEmailConfigured) {
            logger.error("Email service not configured - Cannot send verification email to: {}", toEmail);
            throw new EmailServiceException("Email service not configured. Please contact support.");
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // Use configured from email or fallback
            String from = fromEmail != null && !fromEmail.trim().isEmpty() 
                ? fromEmail 
                : (mailUsername != null && !mailUsername.trim().isEmpty() ? mailUsername : "no-reply@aifitness.com");
            message.setFrom(from);
            message.setTo(toEmail);
            message.setSubject("Verify Your Email - AI Fitness");
            message.setText(buildVerificationEmailBody(verificationCode));
            
            mailSender.send(message);
            
            // Log that email was sent, but do NOT log the verification code
            logger.info("Verification email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send verification email to: {}", toEmail, e);
            logger.error("Email sending exception cause: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            throw new EmailServiceException("Failed to send verification email", e);
        }
    }
    
    /**
     * Builds the email body for verification email.
     * 
     * @param verificationCode The 6-digit verification code
     * @return The email body text
     */
    private String buildVerificationEmailBody(String verificationCode) {
        return String.format(
            "Welcome to AI Fitness!\n\n" +
            "Please verify your email address by entering the following code:\n\n" +
            "%s\n\n" +
            "This code will expire in 15 minutes.\n\n" +
            "If you didn't create an account, please ignore this email.\n\n" +
            "Best regards,\n" +
            "AI Fitness Team",
            verificationCode
        );
    }
}

