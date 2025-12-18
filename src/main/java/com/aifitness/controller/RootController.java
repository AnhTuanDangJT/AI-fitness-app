package com.aifitness.controller;

import com.aifitness.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple root controller so hitting "/" returns a success JSON instead of 404.
 */
@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> root() {
        Map<String, Object> data = new HashMap<>();
        data.put("service", "aifitness-backend");
        data.put("status", "UP");
        data.put("timestamp", Instant.now().toString());
        ApiResponse<Map<String, Object>> response = ApiResponse.success("AI Fitness Backend running", data);
        return ResponseEntity.ok(response);
    }
}

