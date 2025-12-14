package com.aifitness.controller;

import com.aifitness.dto.ApiResponse;
import com.aifitness.dto.MealPreferencesRequest;
import com.aifitness.entity.User;
import com.aifitness.repository.UserRepository;
import com.aifitness.util.JwtTokenService;
import com.aifitness.util.StringSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Meal Preferences Controller
 * 
 * Handles meal preferences endpoints.
 */
@RestController
@RequestMapping("/meal-preferences")
public class MealPreferencesController {
    
    private static final Logger logger = LoggerFactory.getLogger(MealPreferencesController.class);
    
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    
    @Autowired
    public MealPreferencesController(UserRepository userRepository,
                                     JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
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
     * GET /api/meal-preferences
     * 
     * Returns the current user's meal preferences.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<MealPreferencesRequest>> getMealPreferences(HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        Long userId = null;
        
        try {
            logger.info("[RequestId: {}] GET /api/meal-preferences - START", requestId);
            
            User user = getAuthenticatedUser(request);
            userId = user.getId();
            logger.info("[RequestId: {}] Authenticated user: userId={}, username={}", requestId, userId, user.getUsername());
            
            MealPreferencesRequest preferences = new MealPreferencesRequest();
            preferences.setPreferredFoods(user.getPreferredFoods());
            preferences.setDislikedFoods(user.getDislikedFoods());
            preferences.setAllergies(user.getAllergies());
            preferences.setDietaryRestriction(user.getDietaryPreference());
            preferences.setCookingTimePreference(user.getMaxCookingTimePerMeal());
            preferences.setBudgetPerDay(user.getMaxBudgetPerDay());
            preferences.setFavoriteCuisines(user.getFavoriteCuisines());
            
            logger.info("[RequestId: {}] Meal preferences retrieved successfully for userId={}", requestId, userId);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Meal preferences retrieved successfully",
                preferences
            ));
        } catch (RuntimeException e) {
            logger.error("[RequestId: {}] RuntimeException in getMealPreferences for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("[RequestId: {}] Exception in getMealPreferences for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while retrieving meal preferences. Please try again later."));
        }
    }
    
    /**
     * POST /api/meal-preferences
     * 
     * Saves or updates the current user's meal preferences.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> saveMealPreferences(
            @Valid @RequestBody MealPreferencesRequest request,
            HttpServletRequest httpRequest) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        Long userId = null;
        
        try {
            logger.info("[RequestId: {}] POST /api/meal-preferences - START", requestId);
            
            User user = getAuthenticatedUser(httpRequest);
            userId = user.getId();
            logger.info("[RequestId: {}] Authenticated user: userId={}, username={}", requestId, userId, user.getUsername());
            
            // Sanitize string inputs
            if (request.getPreferredFoods() != null) {
                String sanitized = StringSanitizer.validateAndSanitize(request.getPreferredFoods());
                user.setPreferredFoods(sanitized);
            } else {
                user.setPreferredFoods(null);
            }
            
            if (request.getDislikedFoods() != null) {
                String sanitized = StringSanitizer.validateAndSanitize(request.getDislikedFoods());
                user.setDislikedFoods(sanitized);
            } else {
                user.setDislikedFoods(null);
            }
            
            if (request.getAllergies() != null) {
                String sanitized = StringSanitizer.validateAndSanitize(request.getAllergies());
                user.setAllergies(sanitized);
            } else {
                user.setAllergies(null);
            }
            
            if (request.getDietaryRestriction() != null) {
                String sanitized = StringSanitizer.validateAndSanitize(request.getDietaryRestriction());
                user.setDietaryPreference(sanitized);
            } else {
                user.setDietaryPreference(null);
            }
            
            if (request.getFavoriteCuisines() != null) {
                String sanitized = StringSanitizer.validateAndSanitize(request.getFavoriteCuisines());
                user.setFavoriteCuisines(sanitized);
            } else {
                user.setFavoriteCuisines(null);
            }
            
            user.setMaxCookingTimePerMeal(request.getCookingTimePreference());
            user.setMaxBudgetPerDay(request.getBudgetPerDay());
            
            // Save to database
            logger.info("[RequestId: {}] Saving meal preferences to database for userId={}", requestId, userId);
            userRepository.save(user);
            logger.info("[RequestId: {}] Meal preferences saved successfully for userId={}", requestId, userId);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Meal preferences saved successfully",
                "Preferences updated"
            ));
        } catch (RuntimeException e) {
            logger.error("[RequestId: {}] RuntimeException in saveMealPreferences for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("[RequestId: {}] Exception in saveMealPreferences for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while saving meal preferences. Please try again later."));
        }
    }
}



