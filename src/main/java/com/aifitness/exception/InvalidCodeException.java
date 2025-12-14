package com.aifitness.exception;

/**
 * Exception thrown when verification code is invalid (wrong code).
 */
public class InvalidCodeException extends RuntimeException {
    
    public InvalidCodeException(String message) {
        super(message);
    }
    
    public InvalidCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}

