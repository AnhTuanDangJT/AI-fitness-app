package com.aifitness.dto;

import java.time.LocalDate;

/**
 * Daily Meal Plan DTO
 * 
 * Represents a single day's meal plan with breakfast, lunch, dinner, and macros.
 */
public class DailyMealPlanDTO {
    
    private LocalDate date;
    private String breakfast;
    private String lunch;
    private String dinner;
    private Integer calories;
    private Integer protein;
    private Integer carbs;
    private Integer fat;
    
    // Constructors
    public DailyMealPlanDTO() {
    }
    
    public DailyMealPlanDTO(LocalDate date, String breakfast, String lunch, String dinner,
                           Integer calories, Integer protein, Integer carbs, Integer fat) {
        this.date = date;
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }
    
    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public String getBreakfast() {
        return breakfast;
    }
    
    public void setBreakfast(String breakfast) {
        this.breakfast = breakfast;
    }
    
    public String getLunch() {
        return lunch;
    }
    
    public void setLunch(String lunch) {
        this.lunch = lunch;
    }
    
    public String getDinner() {
        return dinner;
    }
    
    public void setDinner(String dinner) {
        this.dinner = dinner;
    }
    
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
    
    public Integer getFat() {
        return fat;
    }
    
    public void setFat(Integer fat) {
        this.fat = fat;
    }
}


