package com.aifitness.exception;

/**
 * Custom exception for when a resource already exists (e.g., duplicate username/email).
 */
public class ResourceAlreadyExistsException extends RuntimeException {
    
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
    
    public ResourceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

