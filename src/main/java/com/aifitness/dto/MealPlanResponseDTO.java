package com.aifitness.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Meal Plan Response DTO
 * 
 * Response structure for meal plan endpoints.
 */
public class MealPlanResponseDTO {
    
    private Long id;
    private Long userId;
    private LocalDate weekStartDate;
    private List<MealPlanEntryDTO> entries;
    private LocalDateTime createdAt;
    
    // Daily summary (calculated)
    private DailyMacrosDTO dailyTargets;
    
    // Constructors
    public MealPlanResponseDTO() {
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }
    
    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }
    
    public List<MealPlanEntryDTO> getEntries() {
        return entries;
    }
    
    public void setEntries(List<MealPlanEntryDTO> entries) {
        this.entries = entries;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public DailyMacrosDTO getDailyTargets() {
        return dailyTargets;
    }
    
    public void setDailyTargets(DailyMacrosDTO dailyTargets) {
        this.dailyTargets = dailyTargets;
    }
}











