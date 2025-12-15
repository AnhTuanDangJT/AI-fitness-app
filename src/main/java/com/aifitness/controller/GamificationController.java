package com.aifitness.controller;

import com.aifitness.dto.ApiResponse;
import com.aifitness.entity.User;
import com.aifitness.repository.UserRepository;
import com.aifitness.service.GamificationService;
import com.aifitness.util.JwtTokenService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Gamification Controller
 * 
 * Handles gamification status and rewards endpoints.
 */
@RestController
@RequestMapping("/gamification")
public class GamificationController {
    
    private final GamificationService gamificationService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public GamificationController(GamificationService gamificationService,
                                  JwtTokenService jwtTokenService,
                                  UserRepository userRepository,
                                  ObjectMapper objectMapper) {
        this.gamificationService = gamificationService;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
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
     * GET /api/gamification/status
     * 
     * Returns the current gamification status for the authenticated user.
     * 
     * Authentication: Required (JWT token in Authorization header)
     * 
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Gamification status retrieved successfully",
     *   "data": {
     *     "xp": 150,
     *     "currentStreakDays": 5,
     *     "longestStreakDays": 10,
     *     "badges": ["FIRST_LOG", "STREAK_3", "XP_100"]
     *   },
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     * 
     * Error Response (401 Unauthorized):
     * {
     *   "success": false,
     *   "message": "Unauthorized: No token provided",
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<GamificationStatusResponse>> getStatus(HttpServletRequest request) {
        try {
            // Get authenticated user
            User user = getAuthenticatedUser(request);
            
            // Parse badges JSON string into array
            Set<String> badges = parseBadges(user.getBadges());
            
            // Check if daily challenge was completed today
            boolean dailyChallengeCompleted = gamificationService.isDailyChallengeCompletedToday(user);
            
            // Create response
            GamificationStatusResponse status = new GamificationStatusResponse(
                user.getXp(),
                user.getCurrentStreakDays(),
                user.getLongestStreakDays(),
                badges.stream().sorted().toList(),
                dailyChallengeCompleted
            );
            
            return ResponseEntity.ok(ApiResponse.success(
                "Gamification status retrieved successfully",
                status
            ));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while retrieving gamification status: " + e.getMessage()));
        }
    }
    
    /**
     * POST /api/gamification/daily-challenge
     * 
     * Records a daily challenge completion event.
     * 
     * IDEMPOTENT: A user can earn DAILY_CHALLENGE XP only ONCE per calendar day.
     * Uses uniqueness constraint: (user_id, type=DAILY_CHALLENGE_COMPLETED, sourceId=yyyy-MM-dd)
     * 
     * Authentication: Required (JWT token in Authorization header)
     * 
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Daily challenge completed successfully",
     *   "data": {
     *     "xp": 10,
     *     "message": "Daily challenge completed! +10 XP"
     *   }
     * }
     * 
     * Error Response (400 Bad Request):
     * {
     *   "success": false,
     *   "message": "Daily challenge already completed today"
     * }
     */
    @PostMapping("/daily-challenge")
    public ResponseEntity<ApiResponse<DailyChallengeResponse>> recordDailyChallenge(HttpServletRequest request) {
        try {
            // Get authenticated user
            User user = getAuthenticatedUser(request);
            
            // Record daily challenge (idempotent - will return early if already completed today)
            // This uses UTC date internally for timezone safety
            gamificationService.recordDailyChallenge(user);
            
            // Create response
            DailyChallengeResponse response = new DailyChallengeResponse(
                10, // XP reward for daily challenge
                "Daily challenge completed! +10 XP"
            );
            
            return ResponseEntity.ok(ApiResponse.success(
                "Daily challenge completed successfully",
                response
            ));
            
        } catch (RuntimeException e) {
            // If event already exists, recordDailyChallenge will return early
            // But we'll still return success since the challenge was already completed
            if (e.getMessage() != null && e.getMessage().contains("already")) {
                DailyChallengeResponse response = new DailyChallengeResponse(
                    0,
                    "Daily challenge already completed today"
                );
                return ResponseEntity.ok(ApiResponse.success(
                    "Daily challenge already completed",
                    response
                ));
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while recording daily challenge: " + e.getMessage()));
        }
    }
    
    /**
     * Parses badges JSON string into Set<String>.
     */
    private Set<String> parseBadges(String badgesJson) {
        if (badgesJson == null || badgesJson.trim().isEmpty() || badgesJson.equals("[]")) {
            return new HashSet<>();
        }
        
        try {
            return objectMapper.readValue(badgesJson, new TypeReference<Set<String>>() {});
        } catch (Exception e) {
            // If parsing fails, return empty set
            return new HashSet<>();
        }
    }
    
    /**
     * Response DTO for gamification status
     */
    public static class GamificationStatusResponse {
        private int xp;
        private int currentStreakDays;
        private int longestStreakDays;
        private java.util.List<String> badges;
        private boolean dailyChallengeCompleted;
        
        public GamificationStatusResponse() {
        }
        
        public GamificationStatusResponse(int xp, int currentStreakDays, int longestStreakDays, java.util.List<String> badges, boolean dailyChallengeCompleted) {
            this.xp = xp;
            this.currentStreakDays = currentStreakDays;
            this.longestStreakDays = longestStreakDays;
            this.badges = badges;
            this.dailyChallengeCompleted = dailyChallengeCompleted;
        }
        
        public int getXp() {
            return xp;
        }
        
        public void setXp(int xp) {
            this.xp = xp;
        }
        
        public int getCurrentStreakDays() {
            return currentStreakDays;
        }
        
        public void setCurrentStreakDays(int currentStreakDays) {
            this.currentStreakDays = currentStreakDays;
        }
        
        public int getLongestStreakDays() {
            return longestStreakDays;
        }
        
        public void setLongestStreakDays(int longestStreakDays) {
            this.longestStreakDays = longestStreakDays;
        }
        
        public java.util.List<String> getBadges() {
            return badges;
        }
        
        public void setBadges(java.util.List<String> badges) {
            this.badges = badges;
        }
        
        public boolean isDailyChallengeCompleted() {
            return dailyChallengeCompleted;
        }
        
        public void setDailyChallengeCompleted(boolean dailyChallengeCompleted) {
            this.dailyChallengeCompleted = dailyChallengeCompleted;
        }
    }
    
    /**
     * Response DTO for daily challenge completion
     */
    public static class DailyChallengeResponse {
        private int xp;
        private String message;
        
        public DailyChallengeResponse() {
        }
        
        public DailyChallengeResponse(int xp, String message) {
            this.xp = xp;
            this.message = message;
        }
        
        public int getXp() {
            return xp;
        }
        
        public void setXp(int xp) {
            this.xp = xp;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}

