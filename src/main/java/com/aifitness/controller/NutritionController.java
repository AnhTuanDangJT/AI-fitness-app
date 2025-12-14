package com.aifitness.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Nutrition Controller
 * 
 * Handles endpoints for nutrition calculations and recommendations.
 * 
 * LOGIC TO MOVE FROM Infoclient.java and mainOne.java:
 * - Calorie goal calculation (mainOne.java lines 293-295)
 * - Macronutrients display (mainOne.java lines 297-302)
 * - Micronutrients display (mainOne.java lines 304-313)
 * 
 * Calculation methods from Infoclient.java:
 * - Calorie calculation based on TDEE and goal (constructor lines 125-136)
 * - Protein calculation: setProtein() method (lines 186-201)
 * - Fat and Carb calculations (constructor lines 138-139)
 * - Micronutrient setup: NutritionBasedOnSex() method (lines 155-184)
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:3000")
public class NutritionController {
    
    /**
     * GET /api/user/nutrition
     * 
     * Returns complete nutrition plan (calories, macros, micros).
     * 
     * LOGIC TO MOVE FROM mainOne.java:
     * - Complete nutrition display (lines 293-313)
     */
    @GetMapping("/nutrition")
    public ResponseEntity<?> getNutrition() {
        // TODO: Implement complete nutrition retrieval
        return ResponseEntity.ok("Nutrition endpoint - to be implemented");
    }
    
    /**
     * GET /api/user/calories
     * 
     * Returns daily calorie goal.
     * 
     * LOGIC TO MOVE FROM Infoclient.java:
     * - Calorie calculation in constructor (lines 125-136)
     * - changecaloGoal() method (lines 243-259)
     */
    @GetMapping("/calories")
    public ResponseEntity<?> getCalories() {
        // TODO: Implement calorie goal retrieval
        return ResponseEntity.ok("Calories endpoint - to be implemented");
    }
    
    /**
     * GET /api/user/macros
     * 
     * Returns macronutrients (Protein, Fat, Carbohydrates).
     * 
     * LOGIC TO MOVE FROM Infoclient.java:
     * - setProtein() method (lines 186-201)
     * - Fat calculation (constructor line 138: 0.8 * weight)
     * - Carb calculation (constructor line 139)
     */
    @GetMapping("/macros")
    public ResponseEntity<?> getMacros() {
        // TODO: Implement macros retrieval
        return ResponseEntity.ok("Macros endpoint - to be implemented");
    }
    
    /**
     * GET /api/user/micros
     * 
     * Returns micronutrient requirements.
     * 
     * LOGIC TO MOVE FROM Infoclient.java:
     * - NutritionBasedOnSex() method (lines 155-184)
     * - Sets Iron, Calcium, VitaminD, Magnesium, Zinc, VitaminB12, Potassium, Sodium
     */
    @GetMapping("/micros")
    public ResponseEntity<?> getMicros() {
        // TODO: Implement micronutrients retrieval
        return ResponseEntity.ok("Micros endpoint - to be implemented");
    }
    
    /**
     * GET /api/user/water-fiber
     * 
     * Returns water and fiber requirements.
     * 
     * LOGIC TO MOVE FROM Infoclient.java:
     * - Water: 3.7L/day (Male) or 2.7L/day (Female) - from NutritionBasedOnSex()
     * - Fiber: 38g/day (Male) or 25g/day (Female) - from NutritionBasedOnSex()
     */
    @GetMapping("/water-fiber")
    public ResponseEntity<?> getWaterFiber() {
        // TODO: Implement water and fiber retrieval
        return ResponseEntity.ok("Water-fiber endpoint - to be implemented");
    }
}

