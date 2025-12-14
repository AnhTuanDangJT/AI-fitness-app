package com.aifitness.controller;

import com.aifitness.dto.ApiResponse;
import com.aifitness.dto.AiCoachResponse;
import com.aifitness.dto.ChatRequest;
import com.aifitness.dto.ChatResponse;
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
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.UUID;

/**
 * AI Coach Controller
 * 
 * Handles endpoints for AI-powered fitness coaching.
 * 
 * Currently uses rule-based logic as a placeholder for future LLM integration.
 * The API contract is stable and will remain the same when real AI is integrated.
 */
@RestController
@RequestMapping("/ai/coach")
// CORS is handled globally in SecurityConfig, no need for @CrossOrigin here
public class AiCoachController {
    
    private static final Logger logger = LoggerFactory.getLogger(AiCoachController.class);
    
    private final AiCoachService aiCoachService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    
    @Autowired
    public AiCoachController(AiCoachService aiCoachService,
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
     * GET /api/ai/coach/advice
     * 
     * Generates personalized AI coaching advice based on the user's weekly progress data.
     * 
     * Authentication: Required (JWT token in Authorization header)
     * 
     * This endpoint analyzes the last 8 weeks of weekly progress logs and provides:
     * - A summary of progress trends (weight, sleep, training, calories, stress)
     * - Personalized recommendations based on rule-based logic
     * 
     * Currently uses rule-based logic as a placeholder for future LLM integration.
     * The API contract is stable and will remain the same when real AI is integrated.
     * 
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "AI coach advice generated successfully",
     *   "data": {
     *     "summary": "Weight decreased by 2.5 kg (78.0 kg → 75.5 kg). Average sleep: 7.2 hours/night. Average training sessions: 4.0/week. Calories 150 below target. Average stress level: 5.2/10.",
     *     "recommendations": [
     *       "Weight plateau detected. Increase protein by 15g and reduce carbs by 15g to break through.",
     *       "Your sleep could be improved. Aim for 7-9 hours per night for optimal recovery and performance."
     *     ]
     *   },
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     * 
     * Empty State Response (200 OK - Insufficient Data):
     * {
     *   "success": true,
     *   "message": "AI coach advice generated successfully",
     *   "data": {
     *     "summary": "You need at least 2 weeks of data to get AI coaching. Please log your weekly progress for a few more weeks.",
     *     "recommendations": [
     *       "Continue logging your weekly progress to unlock personalized AI coaching."
     *     ]
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
    @GetMapping("/advice")
    public ResponseEntity<ApiResponse<AiCoachResponse>> getCoachAdvice(HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        Long userId = null;
        
        try {
            logger.info("[RequestId: {}] GET /api/ai/coach/advice - START", requestId);
            
            // Get authenticated user
            User user = getAuthenticatedUser(request);
            userId = user.getId();
            logger.info("[RequestId: {}] Authenticated user: userId={}, username={}", requestId, userId, user.getUsername());
            
            // Generate AI coach advice
            logger.info("[RequestId: {}] Generating AI coach advice for userId={}", requestId, userId);
            AiCoachResponse response = aiCoachService.generateCoachAdvice(user);
            
            // Log response details (without sensitive data)
            logger.info("[RequestId: {}] AI coach advice generated successfully for userId={}. Summary length: {}, Recommendations count: {}", 
                    requestId, userId, 
                    response.getSummary() != null ? response.getSummary().length() : 0,
                    response.getRecommendations() != null ? response.getRecommendations().size() : 0);
            
            // Return success response
            return ResponseEntity.ok(ApiResponse.success(
                    "AI coach advice generated successfully",
                    response
            ));
            
        } catch (RuntimeException e) {
            logger.error("[RequestId: {}] RuntimeException in getCoachAdvice for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("[RequestId: {}] Exception in getCoachAdvice for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while generating AI coach advice. Please try again later."));
        }
    }
    
    /**
     * POST /api/ai/coach/chat
     * 
     * Handles interactive chat requests from users.
     * 
     * Authentication: Required (JWT token in Authorization header)
     * 
     * Request Body:
     * {
     *   "message": "make me a workout plan",
     *   "date": "2024-01-15",  // optional, defaults to today
     *   "context": {}  // optional client hints
     * }
     * 
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Chat response generated successfully",
     *   "data": {
     *     "assistantMessage": "Here's a workout plan based on your profile...",
     *     "actions": ["Log today's workout", "View workout plan"]
     *   },
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     * 
     * Error Response (400 Bad Request):
     * {
     *   "success": false,
     *   "message": "Message is required",
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     */
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> handleChat(
            HttpServletRequest request,
            @Valid @RequestBody ChatRequest chatRequest) {
        
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        Long userId = null;
        
        try {
            logger.info("[RequestId: {}] POST /api/ai/coach/chat - START", requestId);
            
            // Get authenticated user
            User user = getAuthenticatedUser(request);
            userId = user.getId();
            logger.info("[RequestId: {}] Authenticated user: userId={}, username={}", requestId, userId, user.getUsername());
            
            // Validate and set defaults
            String message = chatRequest.getMessage();
            
            // LOG REQUEST PAYLOAD
            logger.info("[RequestId: {}] Received chat request payload: message='{}', date={}", 
                    requestId, message, chatRequest.getDate());
            
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Message is required"));
            }
            
            // Validate message length (prevent abuse)
            if (message.length() > 1000) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Message is too long. Maximum 1000 characters."));
            }
            
            LocalDate date = chatRequest.getDate();
            if (date == null) {
                date = LocalDate.now();
            }
            
            // Validate date (not too far in future or past)
            LocalDate today = LocalDate.now();
            if (date.isAfter(today.plusDays(7)) || date.isBefore(today.minusDays(365))) {
                date = today;
            }
            
            logger.info("[RequestId: {}] Processing chat: date={}, message='{}' (length={})", 
                    requestId, date, message, message.length());
            
            // Process chat request with timeout protection
            ChatResponse response;
            try {
                // Set a timeout for AI processing (30 seconds)
                // In a real implementation with external AI, this would use CompletableFuture with timeout
                response = aiCoachService.handleChat(user, message, date);
            } catch (Exception e) {
                // Check if it's a timeout-related exception in the cause chain
                if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
                    logger.error("[RequestId: {}] Timeout in handleChat for userId={}", requestId, userId);
                    return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                            .body(ApiResponse.error("AI request timed out"));
                }
                // Other exceptions
                logger.error("[RequestId: {}] AI service error in handleChat for userId={}: {}", 
                        requestId, userId, e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("AI service error"));
            }
            
            // Validate response
            if (response == null || response.getAssistantMessage() == null || response.getAssistantMessage().trim().isEmpty()) {
                logger.warn("[RequestId: {}] Empty response from AI Coach for userId={}", requestId, userId);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(ApiResponse.error("AI Coach is temporarily unavailable. Please try again later."));
            }
            
            logger.info("[RequestId: {}] Chat response generated successfully for userId={}. Response length: {}", 
                    requestId, userId, 
                    response.getAssistantMessage() != null ? response.getAssistantMessage().length() : 0);
            
            // Return success response
            return ResponseEntity.ok(ApiResponse.success(
                    "Chat response generated successfully",
                    response
            ));
            
        } catch (RuntimeException e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("Unauthorized")) {
                logger.error("[RequestId: {}] Unauthorized in handleChat for userId={}: {}", 
                        requestId, userId, errorMsg);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Unauthorized. Please log in again."));
            }
            logger.error("[RequestId: {}] RuntimeException in handleChat for userId={}: {}", 
                    requestId, userId, errorMsg, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(errorMsg != null && errorMsg.length() < 200 ? errorMsg : "Invalid request. Please check your input."));
        } catch (Exception e) {
            logger.error("[RequestId: {}] Exception in handleChat for userId={}: {}", 
                    requestId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while processing your message. Please try again later."));
        }
    }
}

