package com.aifitness.service;

import com.aifitness.exception.EmailServiceException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Email Service backed by Resend HTTPS API.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String PROVIDER_NAME = "resend-api";
    private static final String RESEND_BASE_URL = "https://api.resend.com";

    private final Environment environment;
    private final WebClient webClient;

    @Value("${resend.api-key:}")
    private String resendApiKey;

    @Value("${resend.from-email:}")
    private String fromEmail;

    private boolean isEmailConfigured = false;

    public EmailService(Environment environment, WebClient.Builder webClientBuilder) {
        this.environment = environment;
        this.webClient = webClientBuilder
            .baseUrl(RESEND_BASE_URL)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @PostConstruct
    public void checkEmailConfiguration() {
        String activeProfile = environment.getProperty("spring.profiles.active");
        boolean isProduction = "production".equalsIgnoreCase(activeProfile);

        boolean hasApiKey = resendApiKey != null && !resendApiKey.trim().isEmpty();
        boolean hasFrom = fromEmail != null && !fromEmail.trim().isEmpty();

        isEmailConfigured = hasApiKey && hasFrom;

        logger.info("EMAIL_CONFIG_CHECK provider={} api_key_set={} from_set={}",
            PROVIDER_NAME,
            hasApiKey,
            hasFrom
        );

        if (isEmailConfigured) {
            logger.info("EMAIL_CONFIG_OK=true provider={} from={}", PROVIDER_NAME, "***");
        } else {
            StringBuilder missing = new StringBuilder();
            if (!hasApiKey) missing.append("RESEND_API_KEY ");
            if (!hasFrom) missing.append("RESEND_FROM_EMAIL ");

            logger.error("EMAIL_CONFIG_OK=false Missing required environment variables: {}", missing.toString().trim());
            if (isProduction) {
                logger.warn("PRODUCTION MODE: Email service is not configured. " +
                    "Application will start, but email sending will fail until RESEND_API_KEY and RESEND_FROM_EMAIL are set.");
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
        boolean apiKeySet = resendApiKey != null && !resendApiKey.trim().isEmpty();
        boolean fromSet = fromEmail != null && !fromEmail.trim().isEmpty();

        return new EmailConfigStatus(
            isEmailConfigured,
            PROVIDER_NAME,
            true,
            apiKeySet,
            apiKeySet,
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
        ResendEmailPayload payload = new ResendEmailPayload(
            fromEmail,
            new String[]{to},
            subject,
            text
        );

        webClient.post()
            .uri("/emails")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + resendApiKey)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(Void.class)
            .onErrorResume(WebClientResponseException.class, ex -> {
                logger.error("EMAIL_SEND_FAILED_HTTP status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
                return Mono.error(ex);
            })
            .block();
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

    private record ResendEmailPayload(String from, String[] to, String subject, String text) {}
}

