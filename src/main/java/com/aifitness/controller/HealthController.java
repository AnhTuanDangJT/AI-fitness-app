package com.aifitness.controller;

import com.aifitness.dto.ApiResponse;
import com.aifitness.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 *
 * Provides health check endpoints for deployment verification.
 * These endpoints are public (no authentication required).
 *
 * NOTE:
 * - All routes in this controller are mounted under /api/health
 * - Example: GET /api/health, GET /api/health/email
 * - The context-path is already set to /api in application.properties
 */
@RestController
@RequestMapping("/api")
public class HealthController {
    
    private final EmailService emailService;
    
    @Autowired
    public HealthController(EmailService emailService) {
        this.emailService = emailService;
    }
    
    /**
     * Health Check Endpoint
     * 
     * Returns a simple status response to verify the backend is running.
     * 
     * @return Health status response
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "aifitness-backend");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Email Configuration Health Check Endpoint
     *
     * Returns email service configuration status for debugging production email issues.
     * Uses ApiResponse wrapper for consistent API response format.
     *
     * @return Email configuration status wrapped in ApiResponse
     */
    @GetMapping("/health/email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkEmailHealth() {
        try {
            boolean emailConfigured = emailService.isEmailConfigured();  // Check if email service is configured
            EmailService.EmailConfigStatus configStatus = emailService.getEmailConfigStatus();
            
            // Build response data with all email configuration details
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("emailConfigured", emailConfigured);
            responseData.put("provider", configStatus.getProvider());
            responseData.put("hostSet", configStatus.isHostSet());
            responseData.put("userSet", configStatus.isUserSet());
            responseData.put("passSet", configStatus.isPassSet());
            responseData.put("fromSet", configStatus.isFromSet());
            responseData.put("timestamp", Instant.now().toString());
            
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                emailConfigured,
                emailConfigured ? "Email service is configured" : "Email service is not configured",
                responseData
            );
            
            return ResponseEntity.ok(response);  // Return success response
        } catch (Exception e) {
            // Build error response data
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("emailConfigured", false);
            errorData.put("error", e.getMessage());
            errorData.put("timestamp", Instant.now().toString());
            
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                false,
                "Email service configuration check failed: " + e.getMessage(),
                errorData
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);  // Return error response
        }
    }
}

