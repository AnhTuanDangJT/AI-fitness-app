package com.aifitness.controller;

import com.aifitness.dto.ApiResponse;
import com.aifitness.dto.AiContextResponse;
import com.aifitness.dto.AiHistoryResponse;
import com.aifitness.entity.User;
import com.aifitness.repository.UserRepository;
import com.aifitness.ai.AiCoachService;
import com.aifitness.util.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * AI Context Controller
 * 
 * Handles endpoint for retrieving comprehensive user context for AI Coach.
 */
@RestController
@RequestMapping("/ai")
public class AiContextController {
    
    private static final Logger logger = LoggerFactory.getLogger(AiContextController.class);
    
    private final AiCoachService aiCoachService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    
    @Autowired
    public AiContextController(AiCoachService aiCoachService,
                              JwtTokenService jwtTokenService,
                              UserRepository userRepository) {
        this.aiCoachService = aiCoachService;
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
     * GET /api/ai/context
     * 
     * Returns comprehensive user context for AI Coach.
     * 
     * Authentication: Required (JWT token in Authorization header)
     * 
     * Returns a JSON object with:
     * - user: { id, name, goal, gender, height, weight, age, activityLevel }
     * - nutritionTargets: { calories, protein, carbs, fat }
     * - bodyAnalysisLatest: { bmi, whr, bmr, tdee, bodyFatPct, updatedAt }
     * - weeklyProgressLatest: { weight, notes, createdAt }
     * - mealPlanLatest: { weekStart, summary }
     * - mealPreferences: { preferredFoods, dislikedFoods, cuisines, allergies, budget, cookTime }
     * - gamification: { xp, currentStreakDays, longestStreakDays, badges[] }
     * 
     * All fields are null-safe - returns null for missing data.
     */
    @GetMapping("/context")
    public ResponseEntity<ApiResponse<AiContextResponse>> getAiContext(HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        Long userId = null;
        
        try {
            logger.info("[RequestId: {}] GET /api/ai/context - START", requestId);
            
            // Get authenticated user
            User user = getAuthenticatedUser(request);
            userId = user.getId();
            logger.info("[RequestId: {}] Authenticated user: userId={}, username={}", requestId, userId, user.getUsername());
            
            // Build AI context
            AiContextResponse response = aiCoachService.buildAiContext(user);
            
            logger.info("[RequestId: {}] AI context built successfully for userId={}", requestId, userId);
            
            // Return success response
            return ResponseEntity.ok(ApiResponse.success(
                    "AI context retrieved successfully",
                    response
            ));
            
        } catch (RuntimeException e) {
            logger.error("[RequestId: {}] RuntimeException in getAiContext for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("[RequestId: {}] Exception in getAiContext for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while retrieving AI context. Please try again later."));
        }
    }
    
    /**
     * GET /api/ai/history?limit=20
     * 
     * Returns compact history from user's progress data.
     * 
     * Authentication: Required (JWT token in Authorization header)
     * 
     * Query Parameters:
     * - limit: Number of entries to return (default: 20, max: 50)
     * 
     * Returns last N items from:
     * - weekly progress notes
     * - body analysis notes/summary (if any)
     * - recent meal plan summaries (if stored)
     * 
     * Each entry: { type, date, summaryText }
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<AiHistoryResponse>> getAiHistory(
            HttpServletRequest request,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        Long userId = null;
        
        try {
            logger.info("[RequestId: {}] GET /api/ai/history - START, limit={}", requestId, limit);
            
            // Validate limit
            if (limit < 1) limit = 20;
            if (limit > 50) limit = 50;
            
            // Get authenticated user
            User user = getAuthenticatedUser(request);
            userId = user.getId();
            logger.info("[RequestId: {}] Authenticated user: userId={}, username={}", requestId, userId, user.getUsername());
            
            // Build AI history
            AiHistoryResponse response = aiCoachService.buildAiHistory(user, limit);
            
            logger.info("[RequestId: {}] AI history built successfully for userId={}, entries={}", 
                    requestId, userId, response.getEntries() != null ? response.getEntries().size() : 0);
            
            // Return success response
            return ResponseEntity.ok(ApiResponse.success(
                    "AI history retrieved successfully",
                    response
            ));
            
        } catch (RuntimeException e) {
            logger.error("[RequestId: {}] RuntimeException in getAiHistory for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("[RequestId: {}] Exception in getAiHistory for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while retrieving AI history. Please try again later."));
        }
    }
}

