package com.aifitness.exception;

/**
 * Exception thrown when email service is not configured or fails.
 */
public class EmailServiceException extends RuntimeException {
    
    private final String errorType;
    
    public EmailServiceException(String message) {
        super(message);
        this.errorType = "EMAIL_SEND_FAILED";
    }
    
    public EmailServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = "EMAIL_SEND_FAILED";
    }
    
    public EmailServiceException(String message, String errorType) {
        super(message);
        this.errorType = errorType != null ? errorType : "EMAIL_SEND_FAILED";
    }
    
    public EmailServiceException(String message, String errorType, Throwable cause) {
        super(message, cause);
        this.errorType = errorType != null ? errorType : "EMAIL_SEND_FAILED";
    }
    
    public String getErrorType() {
        return errorType;
    }
}


