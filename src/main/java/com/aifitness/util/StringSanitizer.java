package com.aifitness.util;

import java.util.regex.Pattern;

/**
 * String Sanitization Utility
 * 
 * Provides methods to sanitize strings for safe processing.
 * Removes or escapes potentially dangerous characters and patterns.
 * 
 * Security features:
 * - Trims whitespace
 * - Removes control characters
 * - Rejects SQL injection patterns
 * - Rejects XSS patterns
 * - Rejects command injection patterns
 */
public class StringSanitizer {
    
    // Guard rails for obviously malicious payloads while still allowing rich text feedback.
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(;\\s*(drop|truncate)\\s+table|;\\s*delete\\s+from|--|/\\*)"
    );
    
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(?i)(<\\s*/?\\s*(script|iframe|object|embed)[^>]*>|javascript:|onerror\\s*=|onload\\s*=|onclick\\s*=)"
    );
    
    private static final Pattern COMMAND_INJECTION_PATTERN = Pattern.compile(
        "(?i)((\\|\\|)|(\\&\\&)|(`)|\\$\\()"
    );
    
    private static final Pattern ENCODING_PATTERN = Pattern.compile(
        "(?i)(%3C|%3E|&#x)|\\\\x[0-9a-f]{2}"
    );
    
    /**
     * Sanitizes a string by:
     * - Trimming whitespace
     * - Replacing null with empty string
     * - Removing control characters (except newline, tab, carriage return)
     * 
     * @param input The string to sanitize
     * @return Sanitized string, or empty string if input is null
     */
    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        
        // Trim whitespace
        String sanitized = input.trim();
        
        // Remove control characters (except newline, tab, carriage return)
        sanitized = sanitized.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
        
        return sanitized;
    }
    
    /**
     * Sanitizes a string and returns null if the result is empty.
     * Useful for optional fields.
     * 
     * @param input The string to sanitize
     * @return Sanitized string, or null if empty after sanitization
     */
    public static String sanitizeOrNull(String input) {
        String sanitized = sanitize(input);
        return sanitized.isEmpty() ? null : sanitized;
    }
    
    /**
     * Validates and sanitizes a string, rejecting dangerous patterns.
     * 
     * @param input The string to validate and sanitize
     * @return Sanitized string
     * @throws IllegalArgumentException if dangerous patterns are detected
     */
    public static String validateAndSanitize(String input) {
        if (input == null) {
            return "";
        }
        
        String sanitized = sanitize(input);
        
        // Check for dangerous patterns
        if (SQL_INJECTION_PATTERN.matcher(sanitized).find()) {
            throw new IllegalArgumentException("Input contains potentially dangerous content");
        }
        
        if (XSS_PATTERN.matcher(sanitized).find()) {
            throw new IllegalArgumentException("Input contains potentially dangerous content");
        }
        
        if (COMMAND_INJECTION_PATTERN.matcher(sanitized).find()) {
            throw new IllegalArgumentException("Input contains potentially dangerous content");
        }
        
        if (ENCODING_PATTERN.matcher(sanitized).find()) {
            throw new IllegalArgumentException("Input contains potentially dangerous content");
        }
        
        return sanitized;
    }
    
    /**
     * Validates a string for dangerous patterns without sanitizing.
     * Useful when you want to keep the original input but validate it.
     * 
     * @param input The string to validate
     * @throws IllegalArgumentException if dangerous patterns are detected
     */
    public static void validate(String input) {
        if (input == null) {
            return;
        }
        
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            throw new IllegalArgumentException("Input contains potentially dangerous content");
        }
        
        if (XSS_PATTERN.matcher(input).find()) {
            throw new IllegalArgumentException("Input contains potentially dangerous content");
        }
        
        if (COMMAND_INJECTION_PATTERN.matcher(input).find()) {
            throw new IllegalArgumentException("Input contains potentially dangerous content");
        }
        
        if (ENCODING_PATTERN.matcher(input).find()) {
            throw new IllegalArgumentException("Input contains potentially dangerous content");
        }
    }
    
    /**
     * Sanitizes a string for safe display (HTML escaping not included - use proper templating for that).
     * This method focuses on removing dangerous patterns while preserving safe content.
     * 
     * @param input The string to sanitize
     * @return Sanitized string safe for processing
     */
    public static String sanitizeForProcessing(String input) {
        return validateAndSanitize(input);
    }
}
