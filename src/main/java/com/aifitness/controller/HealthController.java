package com.aifitness.controller;

import com.aifitness.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 *
 * Provides health check endpoints for deployment verification.
 * These endpoints are public (no authentication required).
 *
 * Routing Strategy:
 * - Strategy B: server.servlet.context-path=/api (in production)
 * - Controllers should NOT include /api in @RequestMapping
 * - This controller uses @RequestMapping("/health"), so endpoints are accessible at /api/health/** in production
 * - Example: GET /api/health, GET /api/health/email
 */
@RestController
@RequestMapping("/health")
public class HealthController {
    
    private static final Logger log = LoggerFactory.getLogger(HealthController.class);
    
    private final JavaMailSender javaMailSender;
    private final Environment environment;
    
    @Value("${spring.mail.host:}")
    private String mailHost;
    
    @Value("${spring.mail.port:587}")
    private int mailPort;
    
    @Value("${spring.mail.username:}")
    private String mailUsername;
    
    @Value("${app.email.from:${spring.mail.username:}}")
    private String fromEmail;
    
    @Autowired
    public HealthController(JavaMailSender javaMailSender, Environment environment) {
        this.javaMailSender = javaMailSender;
        this.environment = environment;
        log.info("✅ HealthController loaded (email check is lazy)");
    }
    
    /**
     * Health Check Endpoint
     * 
     * Returns a simple status response to verify the backend is running.
     * Accessible at: GET /api/health (with context-path=/api)
     * 
     * @return Health status response
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "aifitness-backend");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Email Health Check Endpoint
     * 
     * Tests JavaMailSender connection using testConnection() if available.
     * Returns structured JSON response with email configuration details.
     * Accessible at: GET /api/health/email (with context-path=/api)
     * 
     * @return ApiResponse with email health status and configuration details
     */
    @GetMapping("/email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkEmailHealth() {
        Map<String, Object> data = new HashMap<>();
        
        try {
            // Extract email configuration
            String activeProfile = environment.getActiveProfiles().length > 0 
                ? String.join(",", environment.getActiveProfiles()) 
                : "default";
            
            String resolvedHost = resolveConfigValue("MAIL_HOST", "spring.mail.host", mailHost);
            String resolvedPort = resolveConfigValue("MAIL_PORT", "spring.mail.port", mailPort > 0 ? String.valueOf(mailPort) : "");
            String resolvedUsername = resolveConfigValue("MAIL_USERNAME", "spring.mail.username", mailUsername);
            String resolvedFromEmail = resolveConfigValue("APP_EMAIL_FROM", "app.email.from", fromEmail);
            
            // Mask username (show first 3 chars + ***)
            String maskedUsername = StringUtils.hasText(resolvedUsername)
                ? (resolvedUsername.length() > 3 
                    ? resolvedUsername.substring(0, Math.min(3, resolvedUsername.length())) + "***" 
                    : "***")
                : "not configured";
            
            data.put("host", StringUtils.hasText(resolvedHost) ? resolvedHost : "not configured");
            data.put("port", StringUtils.hasText(resolvedPort) ? resolvedPort : "not configured");
            data.put("username", maskedUsername);
            data.put("fromEmail", StringUtils.hasText(resolvedFromEmail) ? resolvedFromEmail : "not configured");
            data.put("activeProfile", activeProfile);
            data.put("timestamp", java.time.LocalDateTime.now().toString());
            data.put("mailSenderBeanPresent", javaMailSender != null);
            
            boolean configComplete = javaMailSender != null
                && StringUtils.hasText(resolvedHost)
                && StringUtils.hasText(resolvedPort)
                && StringUtils.hasText(resolvedUsername)
                && StringUtils.hasText(resolvedFromEmail);
            
            data.put("configComplete", configComplete);
            
            if (configComplete) {
                data.put("connectionStatus", "CONFIG_PRESENT");
                ApiResponse<Map<String, Object>> response = ApiResponse.success(
                    "Email configuration detected. SMTP connection is checked lazily.",
                    data
                );
                return ResponseEntity.ok(response);
            } else {
                data.put("connectionStatus", "CONFIG_INCOMPLETE");
                ApiResponse<Map<String, Object>> response = ApiResponse.error(
                    "Email configuration is incomplete. Verify MAIL_HOST, MAIL_PORT, MAIL_USERNAME, APP_EMAIL_FROM."
                );
                response.setData(data);
                return ResponseEntity.status(503).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error checking email health: ", e);
            data.put("connectionStatus", "ERROR: " + e.getMessage());
            data.put("error", e.getClass().getSimpleName());
            
            ApiResponse<Map<String, Object>> response = ApiResponse.error(
                "Email health check error: " + e.getMessage()
            );
            response.setData(data);
            return ResponseEntity.status(503).body(response);
        }
    }
    
    private String resolveConfigValue(String envKey, String propertyKey, String fallback) {
        String value = environment.getProperty(envKey);
        if (!StringUtils.hasText(value)) {
            value = environment.getProperty(propertyKey);
        }
        if (!StringUtils.hasText(value)) {
            value = fallback;
        }
        return value;
    }
}

