package com.aifitness.exception;

/**
 * Exception thrown when email service is not configured or fails.
 */
public class EmailServiceException extends RuntimeException {
    
    public EmailServiceException(String message) {
        super(message);
    }
    
    public EmailServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}


