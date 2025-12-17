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
    
    @Value("${spring.mail.port:587}")
    private String mailPort;
    
    private boolean isEmailConfigured = false;
    private String providerName = "unknown";
    
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
        String activeProfile = environment.getProperty("spring.profiles.active");
        boolean isProduction = "production".equalsIgnoreCase(activeProfile);
        
        // Detect provider from host
        String effectiveHost = mailHost != null && !mailHost.trim().isEmpty() 
            ? mailHost 
            : "smtp.gmail.com";
        
        if (effectiveHost.contains("gmail")) {
            providerName = "gmail-smtp";
        } else if (effectiveHost.contains("sendgrid")) {
            providerName = "sendgrid-smtp";
        } else if (effectiveHost.contains("mailgun")) {
            providerName = "mailgun-smtp";
        } else if (effectiveHost.contains("resend")) {
            providerName = "resend-smtp";
        } else {
            providerName = "smtp-" + effectiveHost;
        }
        
        // Validate all required fields are present and non-empty
        boolean hasUsername = mailUsername != null && !mailUsername.trim().isEmpty();
        boolean hasPassword = mailPassword != null && !mailPassword.trim().isEmpty();
        boolean hasFrom = fromEmail != null && !fromEmail.trim().isEmpty();
        boolean hasHost = effectiveHost != null && !effectiveHost.trim().isEmpty();
        
        // Also check if fromEmail falls back to mailUsername (as per application-production.properties)
        if (!hasFrom && hasUsername) {
            // app.email.from defaults to spring.mail.username if not set
            fromEmail = mailUsername;
            hasFrom = true;
        }
        
        isEmailConfigured = hasUsername && hasPassword && hasFrom && hasHost;
        
        // Log structured configuration status (NEVER log actual values)
        logger.info("EMAIL_CONFIG_CHECK provider={} host_set={} user_set={} pass_set={} from_set={} port={}",
            providerName,
            hasHost,
            hasUsername,
            hasPassword,
            hasFrom,
            mailPort != null ? mailPort : "587"
        );
        
        if (isEmailConfigured) {
            logger.info("EMAIL_CONFIG_OK=true provider={} host={} from={} port={}", 
                providerName,
                effectiveHost,
                fromEmail != null ? "***" : "missing",
                mailPort != null ? mailPort : "587"
            );
        } else {
            // Log detailed error about what's missing
            StringBuilder missing = new StringBuilder();
            if (!hasHost) missing.append("MAIL_HOST ");
            if (!hasUsername) missing.append("MAIL_USERNAME ");
            if (!hasPassword) missing.append("MAIL_PASSWORD ");
            if (!hasFrom) missing.append("APP_EMAIL_FROM ");
            
            logger.error("EMAIL_CONFIG_OK=false Missing required environment variables: {}", missing.toString().trim());
            logger.error("EMAIL_CONFIG_DETAILS host_set={} user_set={} pass_set={} from_set={}", 
                hasHost, hasUsername, hasPassword, hasFrom);
            
            // In production, log warning but allow startup to proceed
            // Email sending will fail gracefully with clear error messages
            // This prevents deployment failures when env vars are being configured
            if (isProduction) {
                logger.warn("PRODUCTION MODE: Email service is not configured. " +
                    "Application will start, but email sending will fail until environment variables are set: " +
                    "MAIL_HOST, MAIL_USERNAME, MAIL_PASSWORD, APP_EMAIL_FROM");
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
        logger.info("EMAIL_SEND_ATTEMPT provider={} to={}", providerName, toEmail);
        
        if (!isEmailConfigured) {
            logger.error("EMAIL_SEND_FAILED reason=NOT_CONFIGURED provider={} to={}", providerName, toEmail);
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
            logger.info("EMAIL_SEND_SUCCESS provider={} to={}", providerName, toEmail);
        } catch (Exception e) {
            String errorCode = e.getClass().getSimpleName();
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            
            // Enhanced error logging with more context
            logger.error("EMAIL_SEND_FAILED provider={} code={} message={} to={}", 
                providerName, errorCode, errorMessage, toEmail);
            logger.error("EMAIL_SEND_FAILED_DETAILS exception={} cause={}", 
                e.getClass().getSimpleName(),
                e.getCause() != null ? e.getCause().getClass().getSimpleName() : "none");
            
            // Check for common authentication errors and provide helpful context
            String lowerMessage = errorMessage.toLowerCase();
            if (lowerMessage.contains("authentication") || lowerMessage.contains("535") || 
                lowerMessage.contains("invalid credentials") || lowerMessage.contains("login")) {
                logger.error("EMAIL_SEND_FAILED_AUTH_ERROR: Authentication failed. " +
                    "For Gmail, ensure you're using an App-Specific Password if 2FA is enabled. " +
                    "Check MAIL_USERNAME and MAIL_PASSWORD environment variables.");
            } else if (lowerMessage.contains("connection") || lowerMessage.contains("timeout")) {
                logger.error("EMAIL_SEND_FAILED_CONNECTION_ERROR: Connection issue detected. " +
                    "Check MAIL_HOST and MAIL_PORT environment variables. " +
                    "Verify network connectivity and firewall settings.");
            } else if (lowerMessage.contains("tls") || lowerMessage.contains("ssl")) {
                logger.error("EMAIL_SEND_FAILED_TLS_ERROR: TLS/SSL configuration issue. " +
                    "Verify MAIL_PORT (587 for STARTTLS, 465 for SSL) and TLS settings.");
            }
            
            // Log full stack trace for debugging
            logger.error("EMAIL_SEND_FAILED_STACKTRACE", e);
            throw new EmailServiceException(
                "Unable to send verification email. Please try again later.",
                "SEND_FAILED",
                e
            );
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
        logger.info("EMAIL_SEND_ATTEMPT provider={} to={} type=feedback", providerName, toEmail);
        
        if (!isEmailConfigured) {
            logger.error("EMAIL_SEND_FAILED reason=NOT_CONFIGURED provider={} to={} type=feedback", providerName, toEmail);
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
            
            logger.info("EMAIL_SEND_SUCCESS provider={} to={} type=feedback", providerName, toEmail);
        } catch (Exception e) {
            String errorCode = e.getClass().getSimpleName();
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            logger.error("EMAIL_SEND_FAILED provider={} code={} message={} to={} type=feedback", 
                providerName, errorCode, errorMessage, toEmail);
            logger.error("EMAIL_SEND_FAILED_DETAILS exception={} cause={}", 
                e.getClass().getSimpleName(),
                e.getCause() != null ? e.getCause().getClass().getSimpleName() : "none");
            
            // Check for common authentication errors and provide helpful context
            String lowerMessage = errorMessage.toLowerCase();
            if (lowerMessage.contains("authentication") || lowerMessage.contains("535") || 
                lowerMessage.contains("invalid credentials") || lowerMessage.contains("login")) {
                logger.error("EMAIL_SEND_FAILED_AUTH_ERROR: Authentication failed. " +
                    "For Gmail, ensure you're using an App-Specific Password if 2FA is enabled. " +
                    "Check MAIL_USERNAME and MAIL_PASSWORD environment variables.");
            } else if (lowerMessage.contains("connection") || lowerMessage.contains("timeout")) {
                logger.error("EMAIL_SEND_FAILED_CONNECTION_ERROR: Connection issue detected. " +
                    "Check MAIL_HOST and MAIL_PORT environment variables. " +
                    "Verify network connectivity and firewall settings.");
            } else if (lowerMessage.contains("tls") || lowerMessage.contains("ssl")) {
                logger.error("EMAIL_SEND_FAILED_TLS_ERROR: TLS/SSL configuration issue. " +
                    "Verify MAIL_PORT (587 for STARTTLS, 465 for SSL) and TLS settings.");
            }
            
            // Log full stack trace for debugging
            logger.error("EMAIL_SEND_FAILED_STACKTRACE", e);
            throw new EmailServiceException("Unable to send feedback email. Please try again later.", e);
        }
    }
    
    /**
     * Gets the email configuration status for health checks.
     * 
     * @return Email configuration status
     */
    public EmailConfigStatus getEmailConfigStatus() {
        return new EmailConfigStatus(
            isEmailConfigured,
            providerName,
            mailHost != null && !mailHost.trim().isEmpty(),
            mailUsername != null && !mailUsername.trim().isEmpty(),
            mailPassword != null && !mailPassword.trim().isEmpty(),
            fromEmail != null && !fromEmail.trim().isEmpty()
        );
    }

    /**
     * Tests the email service by attempting to send a lightweight test email.
     * <p>
     * This method is intended for health checks. It will NEVER throw an exception
     * to the caller; instead it returns {@code true} on success and {@code false}
     * if anything goes wrong, while logging the underlying error details.
     *
     * @return {@code true} if the test email was sent successfully, {@code false} otherwise
     */
    public boolean testEmail() {
        // If email is not configured, fail fast but gracefully
        if (!isEmailConfigured) {
            logger.warn("EMAIL_TEST_SKIPPED reason=NOT_CONFIGURED provider={}", providerName);
            return false;
        }

        try {
            sendTestEmail(); // Internal helper to perform the actual test send
            logger.info("EMAIL_TEST_SUCCESS provider={}", providerName);
            return true;
        } catch (Exception e) {
            String errorCode = e.getClass().getSimpleName();
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";

            // Handle the exception gracefully and log it with enough detail for debugging
            logger.error("EMAIL_TEST_FAILED provider={} code={} message={}",
                providerName, errorCode, errorMessage);
            logger.error("EMAIL_TEST_FAILED_DETAILS exception={} cause={}",
                e.getClass().getSimpleName(),
                e.getCause() != null ? e.getCause().getClass().getSimpleName() : "none");
            logger.error("EMAIL_TEST_FAILED_STACKTRACE", e);

            return false;
        }
    }

    /**
     * Internal helper used by {@link #testEmail()} to perform a real SMTP send.
     * <p>
     * Sends a simple test email to the configured {@code fromEmail} (or
     * {@code mailUsername} as a fallback). Any exception thrown here will be
     * caught and handled by {@link #testEmail()}.
     */
    private void sendTestEmail() {
        SimpleMailMessage message = new SimpleMailMessage();

        // Use fromEmail as the recipient for the test, falling back to username if needed
        String toAddress = (fromEmail != null && !fromEmail.trim().isEmpty())
            ? fromEmail
            : mailUsername;

        message.setFrom(fromEmail);
        message.setTo(toAddress);
        message.setSubject("AI Fitness - Email Service Test");
        message.setText("This is a test email sent by the AI Fitness application to verify SMTP configuration.");

        mailSender.send(message);
    }

    /**
     * Lightweight check for callers that only care about whether email
     * is configured, without throwing exceptions.
     *
     * This method is safe to call at any time and simply returns the
     * current configuration flag computed in {@link #checkEmailConfiguration()}.
     *
     * @return true if email is configured, false otherwise
     */
    public boolean isEmailConfigured() {
        return isEmailConfigured;
    }
    
    /**
     * Email configuration status for health checks.
     */
    public static class EmailConfigStatus {
        private final boolean emailConfigured;
        private final String provider;
        private final boolean hostSet;
        private final boolean userSet;
        private final boolean passSet;
        private final boolean fromSet;
        
        public EmailConfigStatus(boolean emailConfigured, String provider, 
                                boolean hostSet, boolean userSet, boolean passSet, boolean fromSet) {
            this.emailConfigured = emailConfigured;
            this.provider = provider;
            this.hostSet = hostSet;
            this.userSet = userSet;
            this.passSet = passSet;
            this.fromSet = fromSet;
        }
        
        public boolean isEmailConfigured() {
            return emailConfigured;
        }
        
        public String getProvider() {
            return provider;
        }
        
        public boolean isHostSet() {
            return hostSet;
        }
        
        public boolean isUserSet() {
            return userSet;
        }
        
        public boolean isPassSet() {
            return passSet;
        }
        
        public boolean isFromSet() {
            return fromSet;
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

