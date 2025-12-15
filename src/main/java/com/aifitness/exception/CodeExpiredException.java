package com.aifitness.exception;

/**
 * Exception thrown when verification code has expired.
 */
public class CodeExpiredException extends RuntimeException {
    
    public CodeExpiredException(String message) {
        super(message);
    }
    
    public CodeExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}


