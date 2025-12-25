package com.aifitness.exception;

/**
 * Exception thrown when user has exceeded maximum verification attempts.
 */
public class TooManyAttemptsException extends RuntimeException {
    
    public TooManyAttemptsException(String message) {
        super(message);
    }
    
    public TooManyAttemptsException(String message, Throwable cause) {
        super(message, cause);
    }
}









