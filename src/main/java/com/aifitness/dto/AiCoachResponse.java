package com.aifitness.dto;

import java.util.List;

/**
 * AI Coach Response DTO
 * 
 * Response containing AI coach analysis and recommendations.
 */
public class AiCoachResponse {
    
    private String summary;
    private List<String> recommendations;
    
    // Constructors
    public AiCoachResponse() {
    }
    
    public AiCoachResponse(String summary, List<String> recommendations) {
        this.summary = summary;
        this.recommendations = recommendations;
    }
    
    // Getters and Setters
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public List<String> getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
}






