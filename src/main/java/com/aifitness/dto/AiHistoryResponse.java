package com.aifitness.dto;

import java.time.LocalDate;

/**
 * AI History Response DTO
 * 
 * Response object for GET /api/ai/history endpoint.
 * Contains compact history entries from user's progress data.
 */
public class AiHistoryResponse {
    
    private java.util.List<HistoryEntry> entries;
    
    public static class HistoryEntry {
        private String type; // "weekly_progress", "body_analysis", "meal_plan"
        private LocalDate date;
        private String summaryText;
        
        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public String getSummaryText() { return summaryText; }
        public void setSummaryText(String summaryText) { this.summaryText = summaryText; }
    }
    
    // Main class getters and setters
    public java.util.List<HistoryEntry> getEntries() { return entries; }
    public void setEntries(java.util.List<HistoryEntry> entries) { this.entries = entries; }
}








