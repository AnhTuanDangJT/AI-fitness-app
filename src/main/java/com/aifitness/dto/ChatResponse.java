package com.aifitness.dto;

import java.util.List;

/**
 * Chat Response DTO
 * 
 * Response DTO for AI Coach chat endpoint.
 */
public class ChatResponse {
    
    private String assistantMessage;
    private List<String> actions; // Optional suggested actions
    
    // Constructors
    
    public ChatResponse() {
    }
    
    public ChatResponse(String assistantMessage, List<String> actions) {
        this.assistantMessage = assistantMessage;
        this.actions = actions;
    }
    
    // Getters and Setters
    
    public String getAssistantMessage() {
        return assistantMessage;
    }
    
    public void setAssistantMessage(String assistantMessage) {
        this.assistantMessage = assistantMessage;
    }
    
    public List<String> getActions() {
        return actions;
    }
    
    public void setActions(List<String> actions) {
        this.actions = actions;
    }
}










