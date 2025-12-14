package com.aifitness.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Daily Check-In Response DTO
 * 
 * Response DTO for daily check-in entries.
 */
public class DailyCheckInResponse {
    
    private Long id;
    private LocalDate date;
    private Double weight;
    private Integer steps;
    private Boolean workoutDone;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    
    public DailyCheckInResponse() {
    }
    
    public DailyCheckInResponse(Long id, LocalDate date, Double weight, Integer steps,
                               Boolean workoutDone, String notes,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.date = date;
        this.weight = weight;
        this.steps = steps;
        this.workoutDone = workoutDone;
        this.notes = notes;
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


