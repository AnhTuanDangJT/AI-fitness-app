package com.aifitness.controller;

import com.aifitness.dto.ApiResponse;
import com.aifitness.dto.DailyCheckInRequest;
import com.aifitness.dto.DailyCheckInResponse;
import com.aifitness.entity.User;
import com.aifitness.repository.UserRepository;
import com.aifitness.service.DailyCheckInService;
import com.aifitness.util.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Daily Check-In Controller
 * 
 * Handles endpoints for daily check-in logs.
 */
@RestController
@RequestMapping("/ai/coach/checkins")
// CORS is handled globally in SecurityConfig, no need for @CrossOrigin here
public class DailyCheckInController {
    
    private static final Logger logger = LoggerFactory.getLogger(DailyCheckInController.class);
    
    private final DailyCheckInService dailyCheckInService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    
    @Autowired
    public DailyCheckInController(DailyCheckInService dailyCheckInService,
                                  JwtTokenService jwtTokenService,
                                  UserRepository userRepository) {
        this.dailyCheckInService = dailyCheckInService;
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
     * GET /api/ai/coach/checkins?start=YYYY-MM-DD&end=YYYY-MM-DD
     * 
     * Gets daily check-ins for the authenticated user within a date range.
     * 
     * Query Parameters:
     * - start: Start date (YYYY-MM-DD), optional, defaults to 7 days ago
     * - end: End date (YYYY-MM-DD), optional, defaults to today
     * 
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Daily check-ins retrieved successfully",
     *   "data": [
     *     {
     *       "id": 1,
     *       "date": "2024-01-15",
     *       "weight": 75.5,
     *       "steps": 8500,
     *       "workoutDone": true,
     *       "notes": "Feeling good today",
     *       "createdAt": "2024-01-15T10:30:00",
     *       "updatedAt": "2024-01-15T10:30:00"
     *     }
     *   ],
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DailyCheckInResponse>>> getCheckIns(
            HttpServletRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        Long userId = null;
        
        try {
            logger.info("[RequestId: {}] GET /api/ai/coach/checkins - START", requestId);
            
            // Get authenticated user
            User user = getAuthenticatedUser(request);
            userId = user.getId();
            logger.info("[RequestId: {}] Authenticated user: userId={}, username={}", requestId, userId, user.getUsername());
            
            // Set defaults if not provided
            if (end == null) {
                end = LocalDate.now();
            }
            if (start == null) {
                start = end.minusDays(7);
            }
            
            // Validate date range
            if (start.isAfter(end)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Start date must be before or equal to end date"));
            }
            
            // Get check-ins
            List<DailyCheckInResponse> checkIns = dailyCheckInService.getCheckInsForDateRange(user, start, end);
            
            logger.info("[RequestId: {}] Retrieved {} check-ins for userId={}", requestId, checkIns.size(), userId);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "Daily check-ins retrieved successfully",
                    checkIns
            ));
            
        } catch (RuntimeException e) {
            logger.error("[RequestId: {}] RuntimeException in getCheckIns for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("[RequestId: {}] Exception in getCheckIns for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while retrieving check-ins. Please try again later."));
        }
    }
    
    /**
     * POST /api/ai/coach/checkins
     * 
     * Creates or updates a daily check-in for the authenticated user.
     * 
     * Request Body:
     * {
     *   "date": "2024-01-15",
     *   "weight": 75.5,
     *   "steps": 8500,
     *   "workoutDone": true,
     *   "notes": "Feeling good today"
     * }
     * 
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Daily check-in saved successfully",
     *   "data": {
     *     "id": 1,
     *     "date": "2024-01-15",
     *     "weight": 75.5,
     *     "steps": 8500,
     *     "workoutDone": true,
     *     "notes": "Feeling good today",
     *     "createdAt": "2024-01-15T10:30:00",
     *     "updatedAt": "2024-01-15T10:30:00"
     *   },
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DailyCheckInResponse>> saveCheckIn(
            HttpServletRequest request,
            @Valid @RequestBody DailyCheckInRequest checkInRequest) {
        
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        Long userId = null;
        
        try {
            logger.info("[RequestId: {}] POST /api/ai/coach/checkins - START", requestId);
            
            // Get authenticated user
            User user = getAuthenticatedUser(request);
            userId = user.getId();
            logger.info("[RequestId: {}] Authenticated user: userId={}, username={}", requestId, userId, user.getUsername());
            
            // Save check-in
            DailyCheckInResponse response = dailyCheckInService.saveDailyCheckIn(user, checkInRequest);
            
            logger.info("[RequestId: {}] Daily check-in saved successfully for userId={}, date={}", 
                    requestId, userId, checkInRequest.getDate());
            
            return ResponseEntity.ok(ApiResponse.success(
                    "Daily check-in saved successfully",
                    response
            ));
            
        } catch (RuntimeException e) {
            logger.error("[RequestId: {}] RuntimeException in saveCheckIn for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("[RequestId: {}] Exception in saveCheckIn for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while saving check-in. Please try again later."));
        }
    }
}



