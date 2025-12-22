package com.aifitness.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Daily Check-In Request DTO
 * 
 * Request DTO for creating or updating daily check-in entries.
 */
public class DailyCheckInRequest {
    
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    private Double weight; // in kg
    
    private Integer steps;
    
    private Boolean workoutDone;
    
    private String notes;
    
    // Constructors
    
    public DailyCheckInRequest() {
    }
    
    public DailyCheckInRequest(LocalDate date, Double weight, Integer steps, 
                             Boolean workoutDone, String notes) {
        this.date = date;
        this.weight = weight;
        this.steps = steps;
        this.workoutDone = workoutDone;
        this.notes = notes;
    }
    
    // Getters and Setters
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public Double getWeight() {
        return weight;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    public Integer getSteps() {
        return steps;
    }
    
    public void setSteps(Integer steps) {
        this.steps = steps;
    }
    
    public Boolean getWorkoutDone() {
        return workoutDone;
    }
    
    public void setWorkoutDone(Boolean workoutDone) {
        this.workoutDone = workoutDone;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}









