package com.aifitness.controller;

import com.aifitness.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * 
 * Provides health check endpoints for deployment verification.
 * These endpoints are public (no authentication required).
 */
@RestController
@RequestMapping("/health")
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
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "aifitness-backend");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "Backend is running successfully");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Email Configuration Health Check Endpoint
     * 
     * Returns email service configuration status for debugging production email issues.
     * This endpoint is protected and should only be accessible to admins.
     * 
     * Response includes:
     * - emailConfigured: true if all required env vars are present
     * - provider: detected email provider (gmail-smtp, sendgrid-smtp, etc.)
     * 
     * Does NOT expose actual credentials or values.
     * 
     * @return Email configuration status
     */
    @GetMapping("/email")
    public ResponseEntity<Map<String, Object>> emailHealthCheck() {
        EmailService.EmailConfigStatus configStatus = emailService.getEmailConfigStatus();
        
        Map<String, Object> response = new HashMap<>();
        response.put("emailConfigured", configStatus.isEmailConfigured());
        response.put("provider", configStatus.getProvider());
        response.put("hostSet", configStatus.isHostSet());
        response.put("userSet", configStatus.isUserSet());
        response.put("passSet", configStatus.isPassSet());
        response.put("fromSet", configStatus.isFromSet());
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }
}

