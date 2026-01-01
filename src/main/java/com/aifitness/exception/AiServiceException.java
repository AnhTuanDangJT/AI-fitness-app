package com.aifitness.exception;

/**
 * Exception thrown when AI providers fail to return a usable response.
 */
public class AiServiceException extends RuntimeException {

    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

