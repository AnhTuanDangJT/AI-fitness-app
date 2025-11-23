package com.aifitness.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Body Metrics Controller
 * 
 * Handles endpoints for retrieving body metrics calculations.
 * 
 * LOGIC TO MOVE FROM Infoclient.java and mainOne.java:
 * - BMI calculation and display (mainOne.java lines 232-251)
 * - WHR calculation and health assessment (mainOne.java lines 252-275)
 * - Body Fat calculation (mainOne.java lines 276-277)
 * - WHtR calculation and analysis (mainOne.java lines 278-291)
 * 
 * These are calculated values that can be retrieved from the User entity,
 * but these endpoints provide detailed analysis and interpretations.
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:3000")
public class BodyMetricsController {
    
    /**
     * GET /api/user/body-metrics
     * 
     * Returns all body metrics (BMI, WHR, WHtR, Body Fat) with interpretations.
     */
    @GetMapping("/body-metrics")
    public ResponseEntity<?> getBodyMetrics() {
        // TODO: Implement body metrics retrieval
        // Calculate and return BMI, WHR, WHtR, BodyFat with interpretations
        
        return ResponseEntity.ok("Body metrics endpoint - to be implemented");
    }
    
    /**
     * GET /api/user/bmi
     * 
     * Returns BMI value and category.
     * 
     * LOGIC TO MOVE FROM mainOne.java:
     * - BMI display and categorization (lines 232-251)
     */
    @GetMapping("/bmi")
    public ResponseEntity<?> getBMI() {
        // TODO: Implement BMI retrieval with category
        // Use BMI calculation from Infoclient.java constructor (line 96)
        
        return ResponseEntity.ok("BMI endpoint - to be implemented");
    }
    
    /**
     * GET /api/user/whr
     * 
     * Returns WHR value and health assessment.
     * 
     * LOGIC TO MOVE FROM mainOne.java:
     * - WHR calculation and health risk assessment (lines 252-275)
     * - Uses WHR from Infoclient.java (line 97: this.WHR = Waist/Hip)
     */
    @GetMapping("/whr")
    public ResponseEntity<?> getWHR() {
        // TODO: Implement WHR retrieval with health assessment
        
        return ResponseEntity.ok("WHR endpoint - to be implemented");
    }
    
    /**
     * GET /api/user/whtr
     * 
     * Returns WHtR value and risk analysis.
     * 
     * LOGIC TO MOVE FROM mainOne.java:
     * - WHtR calculation and interpretation (lines 278-291)
     * - Uses WHtR from Infoclient.java (line 98: this.WHtR = Waist/height)
     */
    @GetMapping("/whtr")
    public ResponseEntity<?> getWHtR() {
        // TODO: Implement WHtR retrieval with risk analysis
        
        return ResponseEntity.ok("WHtR endpoint - to be implemented");
    }
    
    /**
     * GET /api/user/body-fat
     * 
     * Returns body fat percentage.
     * 
     * LOGIC TO MOVE FROM Infoclient.java:
     * - BodyFatCal() method (lines 146-153)
     */
    @GetMapping("/body-fat")
    public ResponseEntity<?> getBodyFat() {
        // TODO: Implement body fat retrieval
        // Use BodyFatCal() logic from Infoclient.java
        
        return ResponseEntity.ok("Body fat endpoint - to be implemented");
    }
}

