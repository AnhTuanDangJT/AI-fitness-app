package com.aifitness.controller;

import com.aifitness.dto.ApiResponse;
import com.aifitness.dto.FeedbackRequest;
import com.aifitness.entity.User;
import com.aifitness.exception.EmailServiceException;
import com.aifitness.repository.UserRepository;
import com.aifitness.service.EmailService;
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

/**
 * Feedback Controller
 * 
 * Handles user feedback submission.
 */
@RestController
@RequestMapping("/feedback")
public class FeedbackController {
    
    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);
    private static final String FEEDBACK_EMAIL = "dangtuanjt@gmail.com";
    
    private final EmailService emailService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    
    @Autowired
    public FeedbackController(EmailService emailService,
                             JwtTokenService jwtTokenService,
                             UserRepository userRepository) {
        this.emailService = emailService;
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
     * POST /api/feedback
     * 
     * Submits user feedback via email.
     * 
     * Requires JWT authentication.
     * 
     * Request Body:
     * {
     *   "subject": "Optional subject",
     *   "message": "Feedback message (required)"
     * }
     * 
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Feedback submitted successfully",
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> submitFeedback(
            @Valid @RequestBody FeedbackRequest request,
            HttpServletRequest httpRequest) {
        
        User user = null;
        String sanitizedSubject = null;
        String sanitizedMessage = null;

        try {
            // Get authenticated user
            user = getAuthenticatedUser(httpRequest);
            
            // Sanitize input
            sanitizedSubject = request.getSubject() != null 
                ? StringSanitizer.validateAndSanitize(request.getSubject()) 
                : null;
            sanitizedMessage = StringSanitizer.validateAndSanitize(request.getMessage());
            
            // Validate message is not empty after sanitization
            if (sanitizedMessage == null || sanitizedMessage.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Message cannot be empty"));
            }
            
            if (!emailService.isEmailConfigured()) {
                logger.warn("Feedback email service not configured - storing feedback locally for user {}", user.getId());
                logFeedbackLocally(user, sanitizedSubject, sanitizedMessage);
                return ResponseEntity.ok(
                    ApiResponse.success("Feedback submitted successfully")
                );
            }
            
            // Send feedback email
            emailService.sendFeedbackEmail(
                FEEDBACK_EMAIL,
                user.getEmail(),
                sanitizedSubject,
                sanitizedMessage
            );
            
            logger.info("Feedback submitted successfully by user: {} ({})", 
                user.getUsername(), user.getEmail());
            
            return ResponseEntity.ok(
                ApiResponse.success("Feedback submitted successfully")
            );

        } catch (EmailServiceException e) {
            logger.error("Email delivery failed for feedback submission", e);
            logFeedbackLocally(user, sanitizedSubject, sanitizedMessage != null ? sanitizedMessage : request.getMessage());
            return ResponseEntity.ok(
                ApiResponse.success("Feedback submitted successfully")
            );
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Unauthorized")) {
                logger.warn("Unauthorized feedback submission attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized: Authentication required"));
            }
            logger.error("Error submitting feedback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to submit feedback. Please try again later."));
        } catch (Exception e) {
            logger.error("Unexpected error submitting feedback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to submit feedback. Please try again later."));
        }
    }

    /**
     * Stores feedback content in logs when email delivery is unavailable.
     */
    private void logFeedbackLocally(User user, String subject, String message) {
        String safeSubject = subject != null ? subject : "(no subject)";
        String safeMessage = message != null ? message : "(empty message)";
        if (safeMessage.length() > 2000) {
            safeMessage = safeMessage.substring(0, 2000) + "...[truncated]";
        }

        logger.info("FEEDBACK_CAPTURED userId={} username={} email={} subject=\"{}\" message=\"{}\"",
            user != null ? user.getId() : "anonymous",
            user != null ? user.getUsername() : "unknown",
            user != null ? user.getEmail() : "unknown",
            safeSubject,
            safeMessage.replaceAll("\\s+", " ").trim()
        );
    }
}





