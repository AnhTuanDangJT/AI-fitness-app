package com.aifitness.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Weekly Progress Response DTO
 * 
 * Response DTO for weekly progress entries.
 * Excludes sensitive user information.
 */
public class WeeklyProgressResponse {
    
    private Long id;
    private LocalDate weekStartDate;
    private Double weight;
    private Integer sleepHoursPerNightAverage;
    private Integer stressLevel;
    private Integer hungerLevel;
    private Integer energyLevel;
    private Integer trainingSessionsCompleted;
    private Double caloriesAverage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public WeeklyProgressResponse() {
    }
    
    public WeeklyProgressResponse(Long id, LocalDate weekStartDate, Double weight,
                                 Integer sleepHoursPerNightAverage, Integer stressLevel,
                                 Integer hungerLevel, Integer energyLevel,
                                 Integer trainingSessionsCompleted, Double caloriesAverage,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.weekStartDate = weekStartDate;
        this.weight = weight;
        this.sleepHoursPerNightAverage = sleepHoursPerNightAverage;
        this.stressLevel = stressLevel;
        this.hungerLevel = hungerLevel;
        this.energyLevel = energyLevel;
        this.trainingSessionsCompleted = trainingSessionsCompleted;
        this.caloriesAverage = caloriesAverage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }
    
    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }
    
    public Double getWeight() {
        return weight;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    public Integer getSleepHoursPerNightAverage() {
        return sleepHoursPerNightAverage;
    }
    
    public void setSleepHoursPerNightAverage(Integer sleepHoursPerNightAverage) {
        this.sleepHoursPerNightAverage = sleepHoursPerNightAverage;
    }
    
    public Integer getStressLevel() {
        return stressLevel;
    }
    
    public void setStressLevel(Integer stressLevel) {
        this.stressLevel = stressLevel;
    }
    
    public Integer getHungerLevel() {
        return hungerLevel;
    }
    
    public void setHungerLevel(Integer hungerLevel) {
        this.hungerLevel = hungerLevel;
    }
    
    public Integer getEnergyLevel() {
        return energyLevel;
    }
    
    public void setEnergyLevel(Integer energyLevel) {
        this.energyLevel = energyLevel;
    }
    
    public Integer getTrainingSessionsCompleted() {
        return trainingSessionsCompleted;
    }
    
    public void setTrainingSessionsCompleted(Integer trainingSessionsCompleted) {
        this.trainingSessionsCompleted = trainingSessionsCompleted;
    }
    
    public Double getCaloriesAverage() {
        return caloriesAverage;
    }
    
    public void setCaloriesAverage(Double caloriesAverage) {
        this.caloriesAverage = caloriesAverage;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}






