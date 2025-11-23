package com.aifitness.dto;

import jakarta.validation.constraints.*;

/**
 * Profile Update Request DTO
 * 
 * Request body for updating specific profile fields.
 * All fields are optional to allow partial updates.
 */
public class ProfileUpdateRequest {
    
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
    
    @Positive(message = "Weight must be positive")
    @Min(value = 1, message = "Weight must be at least 1 kg")
    private Double weight;
    
    @Positive(message = "Height must be positive")
    @Min(value = 50, message = "Height must be at least 50 cm")
    private Double height;
    
    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 120, message = "Age must not exceed 120")
    private Integer age;
    
    @Positive(message = "Waist must be positive")
    private Double waist;
    
    @Positive(message = "Hip must be positive")
    private Double hip;
    
    private Boolean sex; // true = male, false = female
    
    @Min(value = 1, message = "Activity level must be between 1 and 5")
    @Max(value = 5, message = "Activity level must be between 1 and 5")
    private Integer activityLevel;
    
    @Min(value = 1, message = "Goal must be between 1 and 4")
    @Max(value = 4, message = "Goal must be between 1 and 4")
    private Integer goal;
    
    // Constructors
    public ProfileUpdateRequest() {
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Double getWeight() {
        return weight;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    public Double getHeight() {
        return height;
    }
    
    public void setHeight(Double height) {
        this.height = height;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public Double getWaist() {
        return waist;
    }
    
    public void setWaist(Double waist) {
        this.waist = waist;
    }
    
    public Double getHip() {
        return hip;
    }
    
    public void setHip(Double hip) {
        this.hip = hip;
    }
    
    public Boolean getSex() {
        return sex;
    }
    
    public void setSex(Boolean sex) {
        this.sex = sex;
    }
    
    public Integer getActivityLevel() {
        return activityLevel;
    }
    
    public void setActivityLevel(Integer activityLevel) {
        this.activityLevel = activityLevel;
    }
    
    public Integer getGoal() {
        return goal;
    }
    
    public void setGoal(Integer goal) {
        this.goal = goal;
    }
    
    /**
     * Checks if any fields are provided for update.
     */
    public boolean hasUpdates() {
        return name != null || weight != null || height != null || age != null ||
               waist != null || hip != null || sex != null ||
               activityLevel != null || goal != null;
    }
}

