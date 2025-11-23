package com.aifitness.controller;

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
 * Provides a simple health check endpoint for deployment verification.
 * This endpoint is public (no authentication required).
 */
@RestController
@RequestMapping("/health")
public class HealthController {
    
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
}

