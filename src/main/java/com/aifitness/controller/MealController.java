package com.aifitness.controller;

import com.aifitness.dto.DailyMealPlanDTO;
import com.aifitness.entity.User;
import com.aifitness.repository.UserRepository;
import com.aifitness.service.MealPlanService;
import com.aifitness.util.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Meal Controller
 * 
 * Handles simple meal plan endpoints.
 */
@RestController
@RequestMapping("/api/meal")
public class MealController {
    
    private final MealPlanService mealPlanService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    
    @Autowired
    public MealController(MealPlanService mealPlanService,
                         JwtTokenService jwtTokenService,
                         UserRepository userRepository) {
        this.mealPlanService = mealPlanService;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
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
     * POST /api/meal/generate-weekly
     * 
     * Generates a weekly meal plan (7 days) with hardcoded mock data.
     * Returns a simple format with breakfast, lunch, dinner, and macros for each day.
     * 
     * Requires JWT authentication.
     * 
     * Response (200 OK):
     * [
     *   {
     *     "date": "2024-01-15",
     *     "breakfast": "Oatmeal with banana",
     *     "lunch": "Grilled chicken + rice",
     *     "dinner": "Salmon with veggies",
     *     "calories": 1450,
     *     "protein": 97,
     *     "carbs": 175,
     *     "fat": 44
     *   },
     *   ...
     * ]
     */
    @PostMapping("/generate-weekly")
    public ResponseEntity<List<DailyMealPlanDTO>> generateWeeklyMealPlan(HttpServletRequest request) {
        try {
            // Validate JWT token (but don't require complete profile)
            User user = getAuthenticatedUser(request);
            
            // Generate weekly meal plan (hardcoded mock data)
            List<DailyMealPlanDTO> weeklyPlan = mealPlanService.generateWeeklyMealPlanSimple();
            
            return ResponseEntity.ok(weeklyPlan);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}


