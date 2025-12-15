package com.aifitness.controller;

import com.aifitness.dto.ApiResponse;
import com.aifitness.dto.WeeklyProgressRequest;
import com.aifitness.dto.WeeklyProgressResponse;
import com.aifitness.entity.EventType;
import com.aifitness.entity.User;
import com.aifitness.repository.UserRepository;
import com.aifitness.service.GamificationService;
import com.aifitness.service.WeeklyProgressService;
import com.aifitness.util.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Weekly Progress Controller
 * 
 * Handles endpoints for weekly progress logging.
 * This data will be used by the AI coach to analyze trends and provide insights.
 */
@RestController
@RequestMapping("/progress/weekly")
// CORS is handled globally in SecurityConfig, no need for @CrossOrigin here
public class WeeklyProgressController {
    
    private final WeeklyProgressService weeklyProgressService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;
    
    @Autowired
    public WeeklyProgressController(WeeklyProgressService weeklyProgressService,
                                   JwtTokenService jwtTokenService,
                                   UserRepository userRepository,
                                   GamificationService gamificationService) {
        this.weeklyProgressService = weeklyProgressService;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.gamificationService = gamificationService;
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
     * POST /api/progress/weekly
     * 
     * Saves or updates a weekly progress entry for the authenticated user.
     * 
     * If a progress entry already exists for the same week, it will be updated.
     * Otherwise, a new entry will be created.
     * 
     * Authentication: Required (JWT token in Authorization header)
     * 
     * Request Body:
     * {
     *   "weekStartDate": "2024-01-01",
     *   "weight": 75.5,
     *   "sleepHoursPerNightAverage": 7,
     *   "stressLevel": 5,
     *   "hungerLevel": 6,
     *   "energyLevel": 7,
     *   "trainingSessionsCompleted": 4,
     *   "caloriesAverage": 2200.0
     * }
     * 
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Weekly progress saved successfully",
     *   "data": {
     *     "id": 1,
     *     "weekStartDate": "2024-01-01",
     *     "weight": 75.5,
     *     ...
     *   },
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     * 
     * Error Response (400 Bad Request - Validation Error):
     * {
     *   "success": false,
     *   "message": "Validation failed",
     *   "data": {
     *     "stressLevel": "Stress level must be between 1 and 10",
     *     "sleepHoursPerNightAverage": "Sleep hours must be between 0 and 24"
     *   },
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WeeklyProgressResponse>> saveWeeklyProgress(
            @Valid @RequestBody WeeklyProgressRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            // Get authenticated user
            User user = getAuthenticatedUser(httpRequest);
            
            // Save weekly progress
            WeeklyProgressResponse response = weeklyProgressService.saveWeeklyProgress(user, request);
            
            // Record gamification event (AFTER successful save)
            // Note: activityDate parameter is ignored - GamificationService always uses UTC today
            try {
                gamificationService.recordEvent(
                    user,
                    EventType.WEEKLY_PROGRESS,
                    response.getId().toString(),
                    java.time.LocalDate.now(java.time.ZoneOffset.UTC)
                );
            } catch (Exception e) {
                // Log but don't fail the request if gamification fails
                System.err.println("Error recording gamification event: " + e.getMessage());
            }
            
            // Return success response
            return ResponseEntity.ok(ApiResponse.success(
                    "Weekly progress saved successfully",
                    response
            ));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while saving weekly progress: " + e.getMessage()));
        }
    }
    
    /**
     * GET /api/progress/weekly/recent?weeks=8
     * 
     * Retrieves recent weekly progress entries for the authenticated user.
     * 
     * Authentication: Required (JWT token in Authorization header)
     * 
     * Query Parameters:
     * - weeks (optional): Number of weeks to retrieve (default: 8, max: 52)
     * 
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Recent weekly progress retrieved successfully",
     *   "data": [
     *     {
     *       "id": 1,
     *       "weekStartDate": "2024-01-08",
     *       "weight": 75.5,
     *       ...
     *     },
     *     {
     *       "id": 2,
     *       "weekStartDate": "2024-01-01",
     *       "weight": 76.0,
     *       ...
     *     }
     *   ],
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
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<WeeklyProgressResponse>>> getRecentProgress(
            @RequestParam(value = "weeks", defaultValue = "8") int weeks,
            HttpServletRequest request) {
        
        try {
            // Get authenticated user
            User user = getAuthenticatedUser(request);
            
            // Get recent progress
            List<WeeklyProgressResponse> progressList = weeklyProgressService
                    .getRecentProgressForUser(user, weeks);
            
            // Return success response
            return ResponseEntity.ok(ApiResponse.success(
                    "Recent weekly progress retrieved successfully",
                    progressList
            ));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while retrieving weekly progress: " + e.getMessage()));
        }
    }
}




