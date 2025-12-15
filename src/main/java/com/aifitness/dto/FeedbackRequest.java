package com.aifitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Feedback Request DTO
 * 
 * Request body for submitting user feedback.
 */
public class FeedbackRequest {
    
    @Size(max = 200, message = "Subject must not exceed 200 characters")
    private String subject;
    
    @NotBlank(message = "Message is required")
    @Size(min = 1, max = 5000, message = "Message must be between 1 and 5000 characters")
    private String message;
    
    public FeedbackRequest() {
    }
    
    public FeedbackRequest(String subject, String message) {
        this.subject = subject;
        this.message = message;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}


