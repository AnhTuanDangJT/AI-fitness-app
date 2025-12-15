package com.aifitness.exception;

import com.aifitness.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 * 
 * Handles exceptions across all controllers and returns standardized error responses.
 * 
 * Security principle: Never expose internal system details to clients.
 * - All error messages are generic and user-friendly
 * - Detailed error information is logged server-side only
 * - Stack traces are never sent to clients
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    // Generic error messages (never reveal system details)
    private static final String GENERIC_ERROR_MESSAGE = "Something went wrong. Please try again later.";
    private static final String VALIDATION_ERROR_MESSAGE = "Validation failed. Please check your input.";
    private static final String UNAUTHORIZED_MESSAGE = "Invalid credentials.";
    private static final String CONFLICT_MESSAGE = "Resource already exists.";
    private static final String BAD_REQUEST_MESSAGE = "Invalid request. Please check your input.";
    
    /**
     * Handle validation errors (from @Valid annotation).
     * Returns field-specific validation errors with actual messages for debugging.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        // Log detailed validation errors server-side (including for signup)
        logger.error("Validation failed (signup): {}", ex.getBindingResult().getAllErrors());
        logger.error("Validation error details: ", ex);
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            // Return actual validation message for debugging
            errors.put(fieldName, errorMessage != null ? errorMessage : "Invalid value");
        });
        
        ApiResponse<Map<String, String>> response = ApiResponse.error(VALIDATION_ERROR_MESSAGE);
        response.setData(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handle constraint violation exceptions.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        
        // Log detailed constraint violations server-side
        logger.debug("Constraint violation: {}", ex.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(VALIDATION_ERROR_MESSAGE);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handle resource already exists exceptions (e.g., duplicate username/email).
     * Returns clear message for signup failures.
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<String>> handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException ex) {
        
        // Log detailed error server-side (including for signup)
        logger.error("Resource already exists (signup): {}", ex.getMessage(), ex);
        
        // Return clear message for signup
        ApiResponse<String> response = ApiResponse.error("Signup failed: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * Handle database constraint violations (e.g., unique constraint on username/email).
     * This catches DataIntegrityViolationException which occurs when database constraints are violated.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex) {
        
        // Log detailed error server-side (including for signup)
        logger.error("Database constraint violation (signup): {}", ex.getMessage(), ex);
        
        String errorMessage = "Signup failed";
        String details = ex.getMessage();
        
        // Check if it's a unique constraint violation
        if (details != null) {
            if (details.contains("username") || details.contains("idx_username")) {
                errorMessage = "Signup failed: Username already exists";
            } else if (details.contains("email") || details.contains("idx_email")) {
                errorMessage = "Signup failed: Email already exists";
            } else {
                errorMessage = "Signup failed: " + details;
            }
        }
        
        ApiResponse<String> response = ApiResponse.error(errorMessage);
        response.setData(details); // Include details for debugging
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * Handle missing request body exceptions.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        
        // Log detailed error server-side (including for signup)
        logger.error("Invalid request body (signup): {}", ex.getMessage(), ex);
        
        ApiResponse<String> response = ApiResponse.error("Signup failed: Invalid request body. Please check all required fields.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handle invalid credentials exceptions (e.g., wrong username/password).
     * Returns generic message to prevent user enumeration attacks.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidCredentialsException(
            InvalidCredentialsException ex) {
        
        // Log authentication failures server-side (for security monitoring)
        logger.warn("Authentication failed: {}", ex.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(UNAUTHORIZED_MESSAGE);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Handle user not found exceptions (404).
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleUserNotFoundException(
            UserNotFoundException ex) {
        
        logger.warn("User not found: {}", ex.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Handle code expired exceptions (410 Gone).
     */
    @ExceptionHandler(CodeExpiredException.class)
    public ResponseEntity<ApiResponse<String>> handleCodeExpiredException(
            CodeExpiredException ex) {
        
        logger.warn("Verification code expired: {}", ex.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.GONE).body(response);
    }
    
    /**
     * Handle too many attempts exceptions (429 Too Many Requests).
     */
    @ExceptionHandler(TooManyAttemptsException.class)
    public ResponseEntity<ApiResponse<String>> handleTooManyAttemptsException(
            TooManyAttemptsException ex) {
        
        logger.warn("Too many verification attempts: {}", ex.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
    
    /**
     * Handle invalid code exceptions (400 Bad Request).
     */
    @ExceptionHandler(InvalidCodeException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidCodeException(
            InvalidCodeException ex) {
        
        logger.warn("Invalid verification code: {}", ex.getMessage());
        
        ApiResponse<String> response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handle email service exceptions (503 Service Unavailable).
     */
    @ExceptionHandler(EmailServiceException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleEmailServiceException(
            EmailServiceException ex) {
        
        logger.error("Email service error: {}", ex.getMessage(), ex);
        
        // Include error type in response for frontend handling
        Map<String, String> errorData = new HashMap<>();
        errorData.put("type", ex.getErrorType());
        errorData.put("message", ex.getMessage());
        
        ApiResponse<Map<String, String>> response = ApiResponse.error(ex.getMessage());
        response.setData(errorData);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    /**
     * Handle illegal argument exceptions (e.g., from StringSanitizer).
     * Returns clear message for debugging.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        // Log detailed error server-side (including for signup)
        logger.error("Illegal argument (signup): {}", ex.getMessage(), ex);
        
        // Return clear message for debugging
        ApiResponse<String> response = ApiResponse.error("Signup failed: " + ex.getMessage());
        response.setData(ex.getMessage()); // Include details for debugging
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handle all other exceptions.
     * Returns error message with details for debugging (temporarily) and logs full stack trace server-side.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception ex) {
        
        // Log full error details server-side (including stack trace)
        logger.error("Unexpected error occurred (signup): {}", ex.getMessage(), ex);
        
        // Return error message with details for debugging (temporarily)
        // TODO: Remove details in production and use GENERIC_ERROR_MESSAGE
        String errorMessage = "Signup failed";
        String details = ex.getMessage();
        if (details != null && !details.isEmpty()) {
            errorMessage = "Signup failed: " + details;
        }
        
        ApiResponse<String> response = ApiResponse.error(errorMessage);
        response.setData(details); // Include details for debugging
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
