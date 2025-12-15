package com.aifitness.controller;

import com.aifitness.dto.ApiResponse;
import com.aifitness.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
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
        boolean emailConfigured = emailService.isEmailConfigured();
        
        // Create response data with email configuration status
        Map<String, Object> emailStatus = new HashMap<>();
        emailStatus.put("emailConfigured", emailConfigured);
        
        // Include detailed status if available
        EmailService.EmailConfigStatus configStatus = emailService.getEmailConfigStatus();
        emailStatus.put("provider", configStatus.isEmailConfigured() ? configStatus.getProvider() : "none");
        emailStatus.put("hostSet", configStatus.isHostSet());
        emailStatus.put("userSet", configStatus.isUserSet());
        emailStatus.put("passSet", configStatus.isPassSet());
        emailStatus.put("fromSet", configStatus.isFromSet());
        
        // Create ApiResponse with success status and data
        ApiResponse<Map<String, Object>> response = ApiResponse.success(
            emailConfigured ? "Email service is configured" : "Email service is not configured",
            emailStatus
        );
        
        return ResponseEntity.ok(response);
    }
}

