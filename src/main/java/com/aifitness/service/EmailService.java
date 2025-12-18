package com.aifitness.service;

import com.aifitness.exception.EmailServiceException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Email Service backed by Gmail SMTP (Spring Mail).
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String PROVIDER_NAME = "gmail-smtp";

    private final Environment environment;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${app.email.from:}")
    private String fromEmail;

    private boolean isEmailConfigured = false;

    public EmailService(Environment environment, JavaMailSender mailSender) {
        this.environment = environment;
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void checkEmailConfiguration() {
        String activeProfile = environment.getProperty("spring.profiles.active", "default");
        boolean isProduction = "production".equalsIgnoreCase(activeProfile);

        boolean hasHost = StringUtils.hasText(mailHost);
        boolean hasUsername = StringUtils.hasText(mailUsername);
        boolean hasPassword = StringUtils.hasText(mailPassword);
        boolean hasFrom = StringUtils.hasText(fromEmail);

        isEmailConfigured = hasHost && hasUsername && hasPassword && hasFrom;

        logger.info("EMAIL_CONFIG_CHECK provider={} host_set={} user_set={} pass_set={} from_set={}",
            PROVIDER_NAME,
            hasHost,
            hasUsername,
            hasPassword,
            hasFrom
        );

        if (isEmailConfigured) {
            logger.info("EMAIL_CONFIG_OK=true provider={} host={} from={}", PROVIDER_NAME, mailHost, "***");
        } else {
            StringBuilder missing = new StringBuilder();
            if (!hasHost) missing.append("MAIL_HOST ");
            if (!hasUsername) missing.append("MAIL_USERNAME ");
            if (!hasPassword) missing.append("MAIL_PASSWORD ");
            if (!hasFrom) missing.append("APP_EMAIL_FROM ");

            logger.error("EMAIL_CONFIG_OK=false Missing required environment variables: {}", missing.toString().trim());
            if (isProduction) {
                logger.warn("PRODUCTION MODE: Email service is not configured. " +
                    "Application will start, but email sending will fail until MAIL_* variables are set.");
            } else {
                logger.warn("DEVELOPMENT MODE: Email service is not configured. Email sending will fail.");
            }
        }
    }

    public void sendVerificationEmail(String toEmail, String verificationCode) {
        logger.info("EMAIL_SEND_ATTEMPT provider={} to={}", PROVIDER_NAME, toEmail);

        if (!isEmailConfigured) {
            logger.error("EMAIL_SEND_FAILED reason=NOT_CONFIGURED provider={} to={}", PROVIDER_NAME, toEmail);
            throw new EmailServiceException("Unable to send verification email. Please try again later.");
        }

        try {
            sendEmail(toEmail, "Verify Your Email - AI Fitness", buildVerificationEmailBody(verificationCode));
            logger.info("EMAIL_SEND_SUCCESS provider={} to={}", PROVIDER_NAME, toEmail);
        } catch (Exception e) {
            handleSendFailure(toEmail, e, "verification");
        }
    }

    public void sendFeedbackEmail(String toEmail, String userEmail, String subject, String message) {
        logger.info("EMAIL_SEND_ATTEMPT provider={} to={} type=feedback", PROVIDER_NAME, toEmail);

        if (!isEmailConfigured) {
            logger.error("EMAIL_SEND_FAILED reason=NOT_CONFIGURED provider={} to={} type=feedback", PROVIDER_NAME, toEmail);
            throw new EmailServiceException("Unable to send feedback email. Please try again later.");
        }

        try {
            String emailSubject = subject != null && !subject.trim().isEmpty()
                ? String.format("[AI Fitness Feedback] %s", subject)
                : "[AI Fitness Feedback] User Feedback";
            sendEmail(toEmail, emailSubject, buildFeedbackEmailBody(userEmail, message));
            logger.info("EMAIL_SEND_SUCCESS provider={} to={} type=feedback", PROVIDER_NAME, toEmail);
        } catch (Exception e) {
            handleSendFailure(toEmail, e, "feedback");
        }
    }

    public EmailConfigStatus getEmailConfigStatus() {
        boolean hostSet = StringUtils.hasText(mailHost);
        boolean userSet = StringUtils.hasText(mailUsername);
        boolean passSet = StringUtils.hasText(mailPassword);
        boolean fromSet = StringUtils.hasText(fromEmail);

        return new EmailConfigStatus(
            isEmailConfigured,
            PROVIDER_NAME,
            hostSet,
            userSet,
            passSet,
            fromSet
        );
    }

    public boolean testEmail() {
        if (!isEmailConfigured) {
            logger.warn("EMAIL_TEST_SKIPPED reason=NOT_CONFIGURED provider={}", PROVIDER_NAME);
            return false;
        }

        try {
            sendTestEmail();
            logger.info("EMAIL_TEST_SUCCESS provider={}", PROVIDER_NAME);
            return true;
        } catch (Exception e) {
            logger.error("EMAIL_TEST_FAILED provider={} message={}", PROVIDER_NAME, e.getMessage(), e);
            return false;
        }
    }

    private void sendTestEmail() {
        sendEmail(
            fromEmail,
            "AI Fitness - Email Service Test",
            "This is a test email sent by the AI Fitness application to verify configuration."
        );
    }

    public boolean isEmailConfigured() {
        return isEmailConfigured;
    }
    
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
    
    private String buildFeedbackEmailBody(String userEmail, String message) {
        return String.format(
            "User Feedback Submission%n%n" +
            "User Email: %s%n" +
            "Timestamp: %s%n%n" +
            "Message:%n%s%n%n" +
            "---%n" +
            "This is an automated message from AI Fitness feedback system.",
            userEmail,
            java.time.LocalDateTime.now().toString(),
            message
        );
    }
    
    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (MailException ex) {
            logger.error("EMAIL_SEND_FAILED_SMTP message={}", ex.getMessage(), ex);
            throw ex;
        }
    }

    private void handleSendFailure(String toEmail, Exception e, String type) {
        String errorCode = e.getClass().getSimpleName();
        String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
        logger.error("EMAIL_SEND_FAILED provider={} type={} code={} message={} to={}",
            PROVIDER_NAME, type, errorCode, errorMessage, toEmail);
        logger.error("EMAIL_SEND_FAILED_STACKTRACE", e);
        throw new EmailServiceException("Unable to send " + type + " email. Please try again later.", e);
    }

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
}

