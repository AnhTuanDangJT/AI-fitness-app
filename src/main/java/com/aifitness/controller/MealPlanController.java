package com.aifitness.controller;

import com.aifitness.dto.ApiResponse;
import com.aifitness.dto.GroceryItem;
import com.aifitness.dto.MealPlanResponseDTO;
import com.aifitness.entity.MealPlan;
import com.aifitness.entity.User;
import com.aifitness.repository.UserRepository;
import com.aifitness.service.MealPlanService;
import com.aifitness.util.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Meal Plan Controller
 * 
 * Handles meal plan generation and retrieval endpoints.
 */
@RestController
@RequestMapping("/ai/meals")
public class MealPlanController {
    
    private static final Logger logger = LoggerFactory.getLogger(MealPlanController.class);
    
    private final MealPlanService mealPlanService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    
    @Autowired
    public MealPlanController(MealPlanService mealPlanService,
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
     * POST /api/ai/meals/generate?weekStart=YYYY-MM-DD
     * 
     * Generates and saves a new meal plan for the specified week.
     * 
     * If weekStart is not provided, defaults to the Monday of the current week.
     * 
     * Request: No body required
     * 
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Meal plan generated successfully",
     *   "data": {
     *     "id": 1,
     *     "userId": 1,
     *     "weekStartDate": "2024-01-15",
     *     "entries": [...],
     *     "dailyTargets": {
     *       "calories": 2000,
     *       "protein": 150,
     *       "carbs": 200,
     *       "fats": 60
     *     },
     *     "createdAt": "2024-01-15T10:30:00"
     *   },
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     * 
     * Error Response (400 Bad Request):
     * {
     *   "success": false,
     *   "message": "User profile is incomplete. Please complete your profile first.",
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<MealPlanResponseDTO>> generateMealPlan(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            HttpServletRequest request) {
        
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        Long userId = null;
        
        try {
            logger.info("[RequestId: {}] POST /api/ai/meals/generate - START. weekStart={}", requestId, weekStart);
            
            // Get authenticated user
            User user = getAuthenticatedUser(request);
            userId = user.getId();
            logger.info("[RequestId: {}] Authenticated user: userId={}, username={}", requestId, userId, user.getUsername());
            
            // If weekStart not provided, default to Monday of current week
            if (weekStart == null) {
                weekStart = getMondayOfCurrentWeek();
                logger.info("[RequestId: {}] weekStart not provided, defaulting to Monday of current week: {}", requestId, weekStart);
            }
            
            // Generate meal plan
            logger.info("[RequestId: {}] Generating meal plan for userId={}, weekStart={}", requestId, userId, weekStart);
            MealPlan mealPlan = mealPlanService.generateWeeklyMealPlanForUser(user, weekStart);
            logger.info("[RequestId: {}] Meal plan generated successfully. MealPlanId={}", requestId, mealPlan.getId());
            
            // Convert to DTO
            MealPlanResponseDTO responseDTO = mealPlanService.toDTO(mealPlan);
            logger.info("[RequestId: {}] Meal plan generated and persisted for userId={}. MealPlanId={}, EntriesCount={}", 
                    requestId, userId, responseDTO.getId(), 
                    responseDTO.getEntries() != null ? responseDTO.getEntries().size() : 0);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Meal plan generated successfully",
                responseDTO
            ));
            
        } catch (RuntimeException e) {
            logger.error("[RequestId: {}] RuntimeException in generateMealPlan for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("[RequestId: {}] Exception in generateMealPlan for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while generating meal plan. Please try again later."));
        }
    }
    
    /**
     * GET /api/ai/meals/current
     * 
     * Returns the latest meal plan for the authenticated user.
     * 
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Meal plan retrieved successfully",
     *   "data": {
     *     "id": 1,
     *     "userId": 1,
     *     "weekStartDate": "2024-01-15",
     *     "entries": [
     *       {
     *         "id": 1,
     *         "date": "2024-01-15",
     *         "mealType": "BREAKFAST",
     *         "name": "Scrambled eggs with whole wheat toast",
     *         "calories": 350,
     *         "protein": 20,
     *         "carbs": 35,
     *         "fats": 12
     *       },
     *       ...
     *     ],
     *     "dailyTargets": {
     *       "calories": 2000,
     *       "protein": 150,
     *       "carbs": 200,
     *       "fats": 60
     *     },
     *     "createdAt": "2024-01-15T10:30:00"
     *   },
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     * 
     * Error Response (404 Not Found):
     * {
     *   "success": false,
     *   "message": "No meal plan found for user",
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<MealPlanResponseDTO>> getCurrentMealPlan(HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        Long userId = null;
        
        try {
            logger.info("[RequestId: {}] GET /api/ai/meals/current - START", requestId);
            
            // Get authenticated user
            User user = getAuthenticatedUser(request);
            userId = user.getId();
            logger.info("[RequestId: {}] Authenticated user: userId={}, username={}", requestId, userId, user.getUsername());
            
            // Get latest meal plan
            logger.info("[RequestId: {}] Fetching latest meal plan for userId={}", requestId, userId);
            MealPlan mealPlan = mealPlanService.getLatestMealPlan(user);
            
            if (mealPlan == null) {
                // No meal plan exists - this is a valid empty state, return 404
                logger.info("[RequestId: {}] No meal plan found for userId={} - returning 404 (empty state)", requestId, userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No meal plan found for user. Please generate a meal plan first."));
            }
            
            // Convert to DTO
            logger.info("[RequestId: {}] Converting MealPlan to DTO for mealPlanId={}", requestId, mealPlan.getId());
            MealPlanResponseDTO responseDTO = mealPlanService.toDTO(mealPlan);
            logger.info("[RequestId: {}] Meal plan retrieved successfully for userId={}. MealPlanId={}, EntriesCount={}", 
                    requestId, userId, responseDTO.getId(), 
                    responseDTO.getEntries() != null ? responseDTO.getEntries().size() : 0);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Meal plan retrieved successfully",
                responseDTO
            ));
            
        } catch (RuntimeException e) {
            logger.error("[RequestId: {}] RuntimeException in getCurrentMealPlan for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("[RequestId: {}] Exception in getCurrentMealPlan for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while retrieving meal plan. Please try again later."));
        }
    }
    
    /**
     * GET /api/ai/meals/grocery-list
     * 
     * Returns the grocery list for the authenticated user's current meal plan.
     * Aggregates ingredients from all meals in the meal plan.
     * 
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Grocery list retrieved successfully",
     *   "data": [
     *     {
     *       "name": "chicken breast",
     *       "quantityText": "200g",
     *       "alreadyHave": false
     *     },
     *     {
     *       "name": "quinoa",
     *       "quantityText": "100g cooked",
     *       "alreadyHave": false
     *     },
     *     ...
     *   ],
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     * 
     * Error Response (404 Not Found):
     * {
     *   "success": false,
     *   "message": "No meal plan found for user. Please generate a meal plan first.",
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     */
    @GetMapping("/grocery-list")
    public ResponseEntity<ApiResponse<List<GroceryItem>>> getGroceryList(HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        Long userId = null;
        
        try {
            logger.info("[RequestId: {}] GET /api/ai/meals/grocery-list - START", requestId);
            
            // Get authenticated user
            User user = getAuthenticatedUser(request);
            userId = user.getId();
            logger.info("[RequestId: {}] Authenticated user: userId={}, username={}", requestId, userId, user.getUsername());
            
            // Check if meal plan exists first
            MealPlan mealPlan = mealPlanService.getLatestMealPlan(user);
            if (mealPlan == null) {
                logger.info("[RequestId: {}] No meal plan found for userId={} - returning 404", requestId, userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No meal plan found for user. Please generate a meal plan first."));
            }
            
            logger.info("[RequestId: {}] Meal plan found for userId={}. MealPlanId={}. Building grocery list...", 
                    requestId, userId, mealPlan.getId());
            
            // Build grocery list (derived from meal plan ingredients)
            List<GroceryItem> groceryList = mealPlanService.buildGroceryListForUser(user);
            
            logger.info("[RequestId: {}] Grocery list built successfully for userId={}. ItemCount={}", 
                    requestId, userId, groceryList.size());
            
            // Return empty list if no ingredients found (meal plan exists but has no ingredient data)
            return ResponseEntity.ok(ApiResponse.success(
                "Grocery list retrieved successfully",
                groceryList
            ));
            
        } catch (RuntimeException e) {
            logger.error("[RequestId: {}] RuntimeException in getGroceryList for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("[RequestId: {}] Exception in getGroceryList for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while retrieving grocery list: " + e.getMessage()));
        }
    }
    
    /**
     * Helper method to get Monday of the current week.
     */
    private LocalDate getMondayOfCurrentWeek() {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        int daysToSubtract = dayOfWeek.getValue() - DayOfWeek.MONDAY.getValue();
        if (daysToSubtract < 0) {
            daysToSubtract += 7; // If today is Sunday, go back 6 days
        }
        return today.minusDays(daysToSubtract);
    }
}

