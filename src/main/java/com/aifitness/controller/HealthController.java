package com.aifitness.controller;

import com.aifitness.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
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
            
            // Mask username (show first 3 chars + ***)
            String maskedUsername = mailUsername != null && !mailUsername.isEmpty()
                ? (mailUsername.length() > 3 
                    ? mailUsername.substring(0, Math.min(3, mailUsername.length())) + "***" 
                    : "***")
                : "not configured";
            
            data.put("host", mailHost != null ? mailHost : "not configured");
            data.put("port", mailPort);
            data.put("username", maskedUsername);
            data.put("fromEmail", fromEmail != null && !fromEmail.isEmpty() ? fromEmail : "not configured");
            data.put("activeProfile", activeProfile);
            data.put("timestamp", java.time.LocalDateTime.now().toString());
            
            // Test email connection
            boolean connectionOk = false;
            String connectionStatus = "unknown";
            
            if (javaMailSender instanceof JavaMailSenderImpl) {
                JavaMailSenderImpl mailSenderImpl = (JavaMailSenderImpl) javaMailSender;
                try {
                    // Use testConnection() to verify SMTP connection
                    mailSenderImpl.testConnection();
                    connectionOk = true;
                    connectionStatus = "OK";
                    log.info("Email health check: Connection test successful");
                } catch (Exception e) {
                    connectionOk = false;
                    connectionStatus = "FAILED: " + e.getMessage();
                    log.warn("Email health check: Connection test failed - {}", e.getMessage());
                }
            } else {
                // Fallback: Try to create a MimeMessage as lightweight validation
                try {
                    javaMailSender.createMimeMessage();
                    connectionOk = true;
                    connectionStatus = "OK (lightweight check)";
                    log.info("Email health check: Lightweight validation successful");
                } catch (Exception e) {
                    connectionOk = false;
                    connectionStatus = "FAILED: " + e.getMessage();
                    log.warn("Email health check: Lightweight validation failed - {}", e.getMessage());
                }
            }
            
            data.put("connectionStatus", connectionStatus);
            
            if (connectionOk) {
                ApiResponse<Map<String, Object>> response = ApiResponse.success(
                    "Email health check OK", 
                    data
                );
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<Map<String, Object>> response = ApiResponse.error(
                    "Email health check failed: " + connectionStatus
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
}

