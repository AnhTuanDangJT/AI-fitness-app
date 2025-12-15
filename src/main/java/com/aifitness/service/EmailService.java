package com.aifitness.service;

import com.aifitness.exception.EmailServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Email Service
 * 
 * Handles sending emails for email verification.
 * All email credentials must come from environment variables.
 * 
 * SECURITY: In production, email configuration is REQUIRED and the service will fail to start
 * if required environment variables are missing. This prevents silent failures.
 */
@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    private final Environment environment;
    
    @Value("${spring.mail.username:}")
    private String mailUsername;
    
    @Value("${spring.mail.password:}")
    private String mailPassword;
    
    @Value("${app.email.from:}")
    private String fromEmail;
    
    @Value("${spring.mail.host:}")
    private String mailHost;
    
    private boolean isEmailConfigured = false;
    
    @Autowired
    public EmailService(JavaMailSender mailSender, Environment environment) {
        this.mailSender = mailSender;
        this.environment = environment;
    }
    
    /**
     * Checks if email service is properly configured on startup.
     * In production mode, this will FAIL FAST if email is not configured.
     * 
     * Required environment variables:
     * - MAIL_USERNAME (maps to spring.mail.username)
     * - MAIL_PASSWORD (maps to spring.mail.password)
     * - APP_EMAIL_FROM (maps to app.email.from)
     * 
     * Optional environment variables:
     * - MAIL_HOST (maps to spring.mail.host, defaults to smtp.gmail.com)
     * - MAIL_PORT (maps to spring.mail.port, defaults to 587)
     */
    @PostConstruct
    public void checkEmailConfiguration() {
        // Check if we're in production mode
        boolean isProduction = "production".equalsIgnoreCase(
            environment.getProperty("spring.profiles.active", "development")
        );
        
        // Validate all required fields are present and non-empty
        boolean hasUsername = mailUsername != null && !mailUsername.trim().isEmpty();
        boolean hasPassword = mailPassword != null && !mailPassword.trim().isEmpty();
        boolean hasFrom = fromEmail != null && !fromEmail.trim().isEmpty();
        
        // Also check if fromEmail falls back to mailUsername (as per application-production.properties)
        if (!hasFrom && hasUsername) {
            // app.email.from defaults to spring.mail.username if not set
            fromEmail = mailUsername;
            hasFrom = true;
        }
        
        isEmailConfigured = hasUsername && hasPassword && hasFrom;
        
        if (isEmailConfigured) {
            logger.info("Email service is configured - Host: {}, From: {}", 
                mailHost != null && !mailHost.trim().isEmpty() ? mailHost : "smtp.gmail.com", 
                fromEmail);
        } else {
            // Log detailed error about what's missing
            StringBuilder missing = new StringBuilder();
            if (!hasUsername) missing.append("MAIL_USERNAME ");
            if (!hasPassword) missing.append("MAIL_PASSWORD ");
            if (!hasFrom) missing.append("APP_EMAIL_FROM ");
            
            String errorMsg = String.format(
                "Email service is NOT configured - Missing required environment variables: %s. " +
                "Set these variables in your production environment.",
                missing.toString().trim()
            );
            
            logger.error(errorMsg);
            logger.error("Current values - username present: {}, password present: {}, from present: {}", 
                hasUsername, hasPassword, hasFrom);
            
            // In production, FAIL FAST - do not allow partial initialization
            if (isProduction) {
                logger.error("PRODUCTION MODE: Email service is REQUIRED. Application startup will fail.");
                throw new IllegalStateException(
                    "Email service configuration is required in production. " +
                    "Please set MAIL_USERNAME, MAIL_PASSWORD, and APP_EMAIL_FROM environment variables."
                );
            } else {
                logger.warn("DEVELOPMENT MODE: Email service is not configured. Email sending will fail.");
            }
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
            throw new EmailServiceException("Unable to send verification email. Please try again later.");
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // Use configured from email (already validated in checkEmailConfiguration)
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Verify Your Email - AI Fitness");
            message.setText(buildVerificationEmailBody(verificationCode));
            
            mailSender.send(message);
            
            // Log that email was sent, but do NOT log the verification code
            logger.info("Verification email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send verification email to: {}", toEmail, e);
            logger.error("Email sending exception cause: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            // Return generic error message to user (never expose internal details)
            throw new EmailServiceException("Unable to send verification email. Please try again later.", e);
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
    
    /**
     * Sends user feedback email to the support email address.
     * 
     * @param toEmail The recipient email address (support email)
     * @param userEmail The email address of the user submitting feedback
     * @param subject Optional subject line
     * @param message The feedback message content
     * @throws EmailServiceException if email service is not configured or sending fails
     */
    public void sendFeedbackEmail(String toEmail, String userEmail, String subject, String message) {
        if (!isEmailConfigured) {
            logger.error("Email service not configured - Cannot send feedback email to: {}", toEmail);
            throw new EmailServiceException("Unable to send feedback email. Please try again later.");
        }
        
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            // Use configured from email (already validated in checkEmailConfiguration)
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(toEmail);
            
            // Build subject
            String emailSubject = subject != null && !subject.trim().isEmpty()
                ? String.format("[AI Fitness Feedback] %s", subject)
                : "[AI Fitness Feedback] User Feedback";
            mailMessage.setSubject(emailSubject);
            
            // Build email body
            mailMessage.setText(buildFeedbackEmailBody(userEmail, message));
            
            mailSender.send(mailMessage);
            
            logger.info("Feedback email sent successfully from user: {} to: {}", userEmail, toEmail);
        } catch (Exception e) {
            logger.error("Failed to send feedback email from: {} to: {}", userEmail, toEmail, e);
            logger.error("Email sending exception cause: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            // Return generic error message to user (never expose internal details)
            throw new EmailServiceException("Unable to send feedback email. Please try again later.", e);
        }
    }
    
    /**
     * Builds the email body for feedback email.
     * 
     * @param userEmail The email address of the user submitting feedback
     * @param message The feedback message content
     * @return The email body text
     */
    private String buildFeedbackEmailBody(String userEmail, String message) {
        return String.format(
            "User Feedback Submission\n\n" +
            "User Email: %s\n" +
            "Timestamp: %s\n\n" +
            "Message:\n" +
            "%s\n\n" +
            "---\n" +
            "This is an automated message from AI Fitness feedback system.",
            userEmail,
            java.time.LocalDateTime.now().toString(),
            message
        );
    }
}

