package com.aifitness.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.mail.internet.MimeMessage;
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
    
    private static final Logger log = LoggerFactory.getLogger(HealthController.class);
    
    private final JavaMailSender javaMailSender;
    
    @Autowired
    public HealthController(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
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
     * Email Health Check Endpoint
     * 
     * Tests JavaMailSender by creating a MimeMessage.
     * Returns "EMAIL_OK" if successful.
     * 
     * @return "EMAIL_OK" if email service is working
     */
    @GetMapping("/health/email")
    public String checkEmailHealth() {
        try {
            // Test JavaMailSender by creating a MimeMessage
            MimeMessage message = javaMailSender.createMimeMessage();
            return "EMAIL_OK";
        } catch (Exception e) {
            log.error("Error checking email health: ", e);
            throw new RuntimeException("Email service check failed: " + e.getMessage(), e);
        }
    }
}

