package com.aifitness.dto;

import java.time.LocalDateTime;

/**
 * Profile Response DTO
 * 
 * Complete profile information response matching Infoclient.java toString() format.
 */
public class ProfileResponseDTO {
    
    // Basic Information
    private Long id;
    private String username;
    private String email;
    private String name;
    private Integer age;
    private String sex;
    
    // Body Measurements
    private Double weight;
    private Double height;
    private Double waist;
    private Double hip;
    
    // Settings
    private Integer activityLevel;
    private String activityLevelName;
    private Integer calorieGoal;
    private String calorieGoalName;
    
    // Food Preferences
    private String dietaryPreference;
    private String dislikedFoods;
    private Integer maxBudgetPerDay;
    private Integer maxCookingTimePerMeal;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Calculated Metrics
    private BodyMetricsDTO bodyMetrics;
    private EnergyDTO energy;
    private MacronutrientsDTO macronutrients;
    private MicronutrientsDTO micronutrients;
    
    // Constructors
    public ProfileResponseDTO() {
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public String getSex() {
        return sex;
    }
    
    public void setSex(String sex) {
        this.sex = sex;
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
    
    public Integer getActivityLevel() {
        return activityLevel;
    }
    
    public void setActivityLevel(Integer activityLevel) {
        this.activityLevel = activityLevel;
    }
    
    public String getActivityLevelName() {
        return activityLevelName;
    }
    
    public void setActivityLevelName(String activityLevelName) {
        this.activityLevelName = activityLevelName;
    }
    
    public Integer getCalorieGoal() {
        return calorieGoal;
    }
    
    public void setCalorieGoal(Integer calorieGoal) {
        this.calorieGoal = calorieGoal;
    }
    
    public String getCalorieGoalName() {
        return calorieGoalName;
    }
    
    public void setCalorieGoalName(String calorieGoalName) {
        this.calorieGoalName = calorieGoalName;
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
    
    public BodyMetricsDTO getBodyMetrics() {
        return bodyMetrics;
    }
    
    public void setBodyMetrics(BodyMetricsDTO bodyMetrics) {
        this.bodyMetrics = bodyMetrics;
    }
    
    public EnergyDTO getEnergy() {
        return energy;
    }
    
    public void setEnergy(EnergyDTO energy) {
        this.energy = energy;
    }
    
    public MacronutrientsDTO getMacronutrients() {
        return macronutrients;
    }
    
    public void setMacronutrients(MacronutrientsDTO macronutrients) {
        this.macronutrients = macronutrients;
    }
    
    public MicronutrientsDTO getMicronutrients() {
        return micronutrients;
    }
    
    public void setMicronutrients(MicronutrientsDTO micronutrients) {
        this.micronutrients = micronutrients;
    }
}

