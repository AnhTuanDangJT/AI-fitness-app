package com.aifitness.dto;

/**
 * Daily Macros DTO
 * 
 * Represents daily macro targets for a meal plan.
 */
public class DailyMacrosDTO {
    
    private Integer calories;
    private Integer protein;
    private Integer carbs;
    private Integer fats;
    
    // Constructors
    public DailyMacrosDTO() {
    }
    
    public DailyMacrosDTO(Integer calories, Integer protein, Integer carbs, Integer fats) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
    }
    
    // Getters and Setters
    public Integer getCalories() {
        return calories;
    }
    
    public void setCalories(Integer calories) {
        this.calories = calories;
    }
    
    public Integer getProtein() {
        return protein;
    }
    
    public void setProtein(Integer protein) {
        this.protein = protein;
    }
    
    public Integer getCarbs() {
        return carbs;
    }
    
    public void setCarbs(Integer carbs) {
        this.carbs = carbs;
    }
    
    public Integer getFats() {
        return fats;
    }
    
    public void setFats(Integer fats) {
        this.fats = fats;
    }
}




