package com.aifitness.controller;

import com.aifitness.dto.*;
import com.aifitness.entity.User;
import com.aifitness.repository.UserRepository;
import com.aifitness.service.BodyMetricsService;
import com.aifitness.service.NutritionService;
import com.aifitness.util.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Calculation Controller
 * 
 * Handles health calculation endpoints.
 * 
 * These endpoints calculate various health metrics based on the authenticated user's profile.
 */
@RestController
@RequestMapping("/calculate")
@CrossOrigin(origins = "http://localhost:3000")
public class CalculationController {
    
    private final UserRepository userRepository;
    private final BodyMetricsService bodyMetricsService;
    private final NutritionService nutritionService;
    private final JwtTokenService jwtTokenService;
    
    @Autowired
    public CalculationController(UserRepository userRepository,
                                 BodyMetricsService bodyMetricsService,
                                 NutritionService nutritionService,
                                 JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.bodyMetricsService = bodyMetricsService;
        this.nutritionService = nutritionService;
        this.jwtTokenService = jwtTokenService;
    }
    
    /**
     * Helper method to extract and validate user from JWT token.
     */
    private User getAuthenticatedUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized: No token provided");
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        if (!jwtTokenService.validateToken(token)) {
            throw new RuntimeException("Unauthorized: Invalid token");
        }
        
        Long userId = jwtTokenService.getUserIdFromToken(token);
        
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * Helper method to check if user has required profile data.
     */
    private void validateProfileData(User user, String requiredFields) {
        if (!user.hasCompleteProfile()) {
            throw new RuntimeException("Profile incomplete. Please complete your profile setup first.");
        }
    }
    
    /**
     * GET /api/calculate/bmi
     * 
     * Calculates BMI (Body Mass Index) for the authenticated user.
     * 
     * Returns:
     * - bmi: The calculated BMI value
     * - category: BMI category (Underweight, Normal, Overweight, Obese, etc.)
     * 
     * Uses weight and height from user's profile.
     */
    @GetMapping("/bmi")
    public ResponseEntity<ApiResponse<BMICalculationResponse>> calculateBMI(HttpServletRequest request) {
        try {
            User user = getAuthenticatedUser(request);
            validateProfileData(user, "weight, height");
            
            double weight = user.getWeight();
            double height = user.getHeight();
            
            // Calculate BMI
            double bmi = bodyMetricsService.calculateBMI(weight, height);
            String category = bodyMetricsService.getBMICategory(bmi);
            
            BMICalculationResponse response = new BMICalculationResponse(bmi, category);
            
            return ResponseEntity.ok(ApiResponse.success("BMI calculated successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while calculating BMI: " + e.getMessage()));
        }
    }
    
    /**
     * GET /api/calculate/whr
     * 
     * Calculates WHR (Waist-to-Hip Ratio) for the authenticated user.
     * 
     * Returns:
     * - whr: The calculated WHR value
     * - riskLevel: Health risk assessment (Good condition, At risk)
     * 
     * Uses waist, hip, and sex from user's profile.
     */
    @GetMapping("/whr")
    public ResponseEntity<ApiResponse<WHRCalculationResponse>> calculateWHR(HttpServletRequest request) {
        try {
            User user = getAuthenticatedUser(request);
            validateProfileData(user, "waist, hip, sex");
            
            double waist = user.getWaist();
            double hip = user.getHip();
            boolean isMale = user.getSex() != null && user.getSex();
            
            // Calculate WHR
            double whr = bodyMetricsService.calculateWHR(waist, hip);
            String riskLevel = bodyMetricsService.assessWHRHealth(whr, isMale);
            
            WHRCalculationResponse response = new WHRCalculationResponse(whr, riskLevel);
            
            return ResponseEntity.ok(ApiResponse.success("WHR calculated successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while calculating WHR: " + e.getMessage()));
        }
    }
    
    /**
     * GET /api/calculate/bmr-tdee
     * 
     * Calculates BMR (Basal Metabolic Rate) and TDEE (Total Daily Energy Expenditure) for the authenticated user.
     * 
     * Returns:
     * - bmr: Basal Metabolic Rate (calories per day at rest)
     * - tdee: Total Daily Energy Expenditure (calories per day including activity)
     * - activityLevelDescription: Description of the activity level
     * 
     * Uses weight, height, age, sex, and activity level from user's profile.
     */
    @GetMapping("/bmr-tdee")
    public ResponseEntity<ApiResponse<BMRTDEECalculationResponse>> calculateBMRTDEE(HttpServletRequest request) {
        try {
            User user = getAuthenticatedUser(request);
            validateProfileData(user, "weight, height, age, sex, activityLevel");
            
            double weight = user.getWeight();
            double height = user.getHeight();
            int age = user.getAge();
            boolean isMale = user.getSex() != null && user.getSex();
            int activityLevel = user.getActivityLevel();
            
            // Calculate BMR
            double bmr = nutritionService.calculateBMR(weight, height, age, isMale);
            
            // Calculate TDEE (based on activity level)
            double tdee = nutritionService.calculateTDEE(bmr, activityLevel);
            
            // Get activity level description
            String activityLevelDescription = getActivityLevelDescription(activityLevel);
            
            BMRTDEECalculationResponse response = new BMRTDEECalculationResponse(bmr, tdee, activityLevelDescription);
            
            return ResponseEntity.ok(ApiResponse.success("BMR and TDEE calculated successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while calculating BMR and TDEE: " + e.getMessage()));
        }
    }
    
    /**
     * GET /api/calculate/nutrition-goal
     * 
     * Calculates nutrition goals (calorie target and protein target) for the authenticated user.
     * 
     * Returns:
     * - calorieTarget: Daily calorie target based on goal (kcal/day)
     * - proteinTarget: Daily protein target based on goal (grams/day)
     * - goalDescription: Description of the fitness goal
     * 
     * Uses weight, height, age, sex, activity level, and calorie goal from user's profile.
     */
    @GetMapping("/nutrition-goal")
    public ResponseEntity<ApiResponse<NutritionGoalCalculationResponse>> calculateNutritionGoal(HttpServletRequest request) {
        try {
            User user = getAuthenticatedUser(request);
            validateProfileData(user, "weight, height, age, sex, activityLevel, calorieGoal");
            
            double weight = user.getWeight();
            double height = user.getHeight();
            int age = user.getAge();
            boolean isMale = user.getSex() != null && user.getSex();
            int activityLevel = user.getActivityLevel();
            int calorieGoal = user.getCalorieGoal();
            
            // Calculate BMR
            double bmr = nutritionService.calculateBMR(weight, height, age, isMale);
            
            // Calculate TDEE
            double tdee = nutritionService.calculateTDEE(bmr, activityLevel);
            
            // Calculate calorie target (based on goal)
            double calorieTarget = nutritionService.calculateGoalCalories(tdee, calorieGoal);
            
            // Calculate protein target (based on goal and weight)
            double proteinTarget = nutritionService.calculateProtein(calorieGoal, weight);
            
            // Get goal description
            String goalDescription = getGoalDescription(calorieGoal);
            
            NutritionGoalCalculationResponse response = new NutritionGoalCalculationResponse(
                    calorieTarget, proteinTarget, goalDescription);
            
            return ResponseEntity.ok(ApiResponse.success("Nutrition goals calculated successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while calculating nutrition goals: " + e.getMessage()));
        }
    }
    
    /**
     * Helper method to get activity level description.
     */
    private String getActivityLevelDescription(int activityLevel) {
        switch (activityLevel) {
            case 1:
                return "Sedentary (no exercise)";
            case 2:
                return "Lightly active (1–3×/week)";
            case 3:
                return "Moderately active (3–5×/week)";
            case 4:
                return "Very active (6–7×/week)";
            case 5:
                return "Extra active (2×/day)";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Helper method to get goal description.
     */
    private String getGoalDescription(int goal) {
        switch (goal) {
            case 1:
                return "Lose weight";
            case 2:
                return "Maintain weight";
            case 3:
                return "Gain muscle";
            case 4:
                return "Gain muscle and lose fat";
            default:
                return "Unknown";
        }
    }
}

