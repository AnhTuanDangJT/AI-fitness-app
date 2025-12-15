package com.aifitness.dto;

import java.time.LocalDate;

/**
 * Meal Plan Entry DTO
 * 
 * Represents a single meal in a meal plan.
 */
public class MealPlanEntryDTO {
    
    private Long id;
    private LocalDate date;
    private String mealType;
    private String name;
    private Integer calories;
    private Integer protein;
    private Integer carbs;
    private Integer fats;
    
    // Constructors
    public MealPlanEntryDTO() {
    }
    
    public MealPlanEntryDTO(Long id, LocalDate date, String mealType, String name,
                           Integer calories, Integer protein, Integer carbs, Integer fats) {
        this.id = id;
        this.date = date;
        this.mealType = mealType;
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
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
    
    public String getMealType() {
        return mealType;
    }
    
    public void setMealType(String mealType) {
        this.mealType = mealType;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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
    
    public Integer getFats() {
        return fats;
    }
    
    public void setFats(Integer fats) {
        this.fats = fats;
    }
}





