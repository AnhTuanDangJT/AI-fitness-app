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
    
    private Map<String, Object> context; // Optional client hints
    
    // Constructors
    
    public ChatRequest() {
    }
    
    public ChatRequest(String message, LocalDate date, Map<String, Object> context) {
        this.message = message;
        this.date = date;
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
    
    public Map<String, Object> getContext() {
        return context;
    }
    
    public void setContext(Map<String, Object> context) {
        this.context = context;
    }
}

