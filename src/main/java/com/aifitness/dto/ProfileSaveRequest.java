package com.aifitness.dto;

import jakarta.validation.constraints.*;

/**
 * Profile Save Request DTO
 * 
 * Request body for saving/updating user profile.
 * 
 * Includes strict input validation to prevent injection attacks.
 */
public class ProfileSaveRequest {
    
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    // Allow letters, spaces, hyphens, apostrophes (for names like O'Brien, Mary-Jane)
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Name can only contain letters, spaces, hyphens, and apostrophes")
    private String name;
    
    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    @Min(value = 1, message = "Weight must be at least 1 kg")
    private Double weight;
    
    @NotNull(message = "Height is required")
    @Positive(message = "Height must be positive")
    @Min(value = 50, message = "Height must be at least 50 cm")
    private Double height;
    
    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 120, message = "Age must not exceed 120")
    private Integer age;
    
    @NotNull(message = "Waist measurement is required")
    @Positive(message = "Waist must be positive")
    private Double waist;
    
    @NotNull(message = "Hip measurement is required")
    @Positive(message = "Hip must be positive")
    private Double hip;
    
    @NotNull(message = "Sex is required")
    private Boolean sex; // true = male, false = female
    
    @NotNull(message = "Activity level is required")
    @Min(value = 1, message = "Activity level must be between 1 and 5")
    @Max(value = 5, message = "Activity level must be between 1 and 5")
    private Integer activityLevel;
    
    @NotNull(message = "Goal is required")
    @Min(value = 1, message = "Goal must be between 1 and 4")
    @Max(value = 4, message = "Goal must be between 1 and 4")
    private Integer goal;
    
    // Food Preferences (optional)
    @Size(max = 50, message = "Dietary preference must not exceed 50 characters")
    private String dietaryPreference;
    
    @Size(max = 500, message = "Disliked foods must not exceed 500 characters")
    private String dislikedFoods;
    
    @Positive(message = "Max budget per day must be positive")
    private Integer maxBudgetPerDay;
    
    @Positive(message = "Max cooking time per meal must be positive")
    @Min(value = 1, message = "Max cooking time per meal must be at least 1 minute")
    private Integer maxCookingTimePerMeal;
    
    // Constructors
    public ProfileSaveRequest() {
    }
    
    public ProfileSaveRequest(String name, Double weight, Double height, Integer age,
                             Double waist, Double hip, Boolean sex,
                             Integer activityLevel, Integer goal) {
        this.name = name;
        this.weight = weight;
        this.height = height;
        this.age = age;
        this.waist = waist;
        this.hip = hip;
        this.sex = sex;
        this.activityLevel = activityLevel;
        this.goal = goal;
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
    
    public String getDietaryPreference() {
        return dietaryPreference;
    }
    
    public void setDietaryPreference(String dietaryPreference) {
        this.dietaryPreference = dietaryPreference;
    }
    
    public String getDislikedFoods() {
        return dislikedFoods;
    }
    
    public void setDislikedFoods(String dislikedFoods) {
        this.dislikedFoods = dislikedFoods;
    }
    
    public Integer getMaxBudgetPerDay() {
        return maxBudgetPerDay;
    }
    
    public void setMaxBudgetPerDay(Integer maxBudgetPerDay) {
        this.maxBudgetPerDay = maxBudgetPerDay;
    }
    
    public Integer getMaxCookingTimePerMeal() {
        return maxCookingTimePerMeal;
    }
    
    public void setMaxCookingTimePerMeal(Integer maxCookingTimePerMeal) {
        this.maxCookingTimePerMeal = maxCookingTimePerMeal;
    }
}

