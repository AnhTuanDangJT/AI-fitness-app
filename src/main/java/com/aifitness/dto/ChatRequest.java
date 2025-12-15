package com.aifitness.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Map;

/**
 * Chat Request DTO
 * 
 * Request DTO for AI Coach chat endpoint.
 */
public class ChatRequest {
    
    @NotBlank(message = "Message is required")
    private String message;
    
    private LocalDate date; // Optional date for context (default: today)
    
    private String language; // UI language (en | vi), defaults to "en"
    
    private Map<String, Object> context; // Optional client hints
    
    // Constructors
    
    public ChatRequest() {
    }
    
    public ChatRequest(String message, LocalDate date, String language, Map<String, Object> context) {
        this.message = message;
        this.date = date;
        this.language = language;
        this.context = context;
    }
    
    // Getters and Setters
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public Map<String, Object> getContext() {
        return context;
    }
    
    public void setContext(Map<String, Object> context) {
        this.context = context;
    }
}

