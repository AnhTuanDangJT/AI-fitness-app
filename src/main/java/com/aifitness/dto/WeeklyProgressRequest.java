package com.aifitness.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Weekly Progress Request DTO
 * 
 * Request body for saving/updating weekly progress entries.
 * 
 * Includes strict input validation to ensure data quality.
 */
public class WeeklyProgressRequest {
    
    @NotNull(message = "Week start date is required")
    private LocalDate weekStartDate;
    
    @Positive(message = "Weight must be positive")
    @Min(value = 1, message = "Weight must be at least 1 kg")
    @Max(value = 500, message = "Weight must not exceed 500 kg")
    private Double weight;
    
    @NotNull(message = "Sleep hours per night average is required")
    @Min(value = 0, message = "Sleep hours must be between 0 and 24")
    @Max(value = 24, message = "Sleep hours must be between 0 and 24")
    private Integer sleepHoursPerNightAverage;
    
    @NotNull(message = "Stress level is required")
    @Min(value = 1, message = "Stress level must be between 1 and 10")
    @Max(value = 10, message = "Stress level must be between 1 and 10")
    private Integer stressLevel;
    
    @NotNull(message = "Hunger level is required")
    @Min(value = 1, message = "Hunger level must be between 1 and 10")
    @Max(value = 10, message = "Hunger level must be between 1 and 10")
    private Integer hungerLevel;
    
    @NotNull(message = "Energy level is required")
    @Min(value = 1, message = "Energy level must be between 1 and 10")
    @Max(value = 10, message = "Energy level must be between 1 and 10")
    private Integer energyLevel;
    
    @NotNull(message = "Training sessions completed is required")
    @Min(value = 0, message = "Training sessions must be 0 or greater")
    @Max(value = 20, message = "Training sessions must not exceed 20 per week")
    private Integer trainingSessionsCompleted;
    
    @Positive(message = "Average calories must be positive")
    @Min(value = 500, message = "Average calories must be at least 500")
    @Max(value = 10000, message = "Average calories must not exceed 10000")
    private Double caloriesAverage;
    
    // Constructors
    public WeeklyProgressRequest() {
    }
    
    public WeeklyProgressRequest(LocalDate weekStartDate, Double weight,
                                Integer sleepHoursPerNightAverage, Integer stressLevel,
                                Integer hungerLevel, Integer energyLevel,
                                Integer trainingSessionsCompleted, Double caloriesAverage) {
        this.weekStartDate = weekStartDate;
        this.weight = weight;
        this.sleepHoursPerNightAverage = sleepHoursPerNightAverage;
        this.stressLevel = stressLevel;
        this.hungerLevel = hungerLevel;
        this.energyLevel = energyLevel;
        this.trainingSessionsCompleted = trainingSessionsCompleted;
        this.caloriesAverage = caloriesAverage;
    }
    
    // Getters and Setters
    
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
}












