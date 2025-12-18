package com.aifitness.controller;

import com.aifitness.dto.ApiResponse;
import com.aifitness.service.EmailService;
import com.aifitness.service.EmailService.EmailConfigStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
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

    private final Environment environment;
    private final EmailService emailService;

    public HealthController(Environment environment, EmailService emailService) {
        this.environment = environment;
        this.emailService = emailService;
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
     * Returns structured JSON response with email configuration details.
     * Accessible at: GET /api/health/email (with context-path=/api)
     * 
     * @return ApiResponse with email health status and configuration details
     */
    @GetMapping("/email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkEmailHealth() {
        Map<String, Object> data = new HashMap<>();
        
        try {
            String activeProfile = environment.getActiveProfiles().length > 0
                ? String.join(",", environment.getActiveProfiles())
                : "default";

            EmailConfigStatus status = emailService.getEmailConfigStatus();

            data.put("provider", status.getProvider());
            data.put("activeProfile", activeProfile);
            data.put("timestamp", java.time.LocalDateTime.now().toString());
            data.put("hostSet", status.isHostSet());
            data.put("userSet", status.isUserSet());
            data.put("passSet", status.isPassSet());
            data.put("fromSet", status.isFromSet());
            data.put("configComplete", status.isEmailConfigured());

            if (status.isEmailConfigured()) {
                data.put("connectionStatus", "CONFIG_PRESENT");
                ApiResponse<Map<String, Object>> response = ApiResponse.success(
                    "Email configuration detected. Connection is handled via SMTP.",
                    data
                );
                return ResponseEntity.ok(response);
            } else {
                data.put("connectionStatus", "CONFIG_INCOMPLETE");
                ApiResponse<Map<String, Object>> response = ApiResponse.error(
                    "Email configuration is incomplete. Verify MAIL_HOST, MAIL_USERNAME, MAIL_PASSWORD, and APP_EMAIL_FROM."
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

