package com.aifitness.dto;

import jakarta.validation.constraints.Size;

/**
 * Meal Preferences Request DTO
 * 
 * Request body for saving meal preferences.
 */
public class MealPreferencesRequest {
    
    @Size(max = 500, message = "Preferred foods must not exceed 500 characters")
    private String preferredFoods; // Comma-separated list
    
    @Size(max = 500, message = "Disliked foods must not exceed 500 characters")
    private String dislikedFoods; // Comma-separated list
    
    @Size(max = 500, message = "Allergies must not exceed 500 characters")
    private String allergies; // Comma-separated list
    
    @Size(max = 50, message = "Dietary restriction must not exceed 50 characters")
    private String dietaryRestriction; // e.g., "omnivore", "vegan", "vegetarian", "halal", "kosher"
    
    private Integer cookingTimePreference; // Maximum cooking time per meal in minutes
    
    private Integer budgetPerDay; // Maximum budget per day
    
    @Size(max = 500, message = "Favorite cuisines must not exceed 500 characters")
    private String favoriteCuisines; // Comma-separated list
    
    // Getters and Setters
    
    public String getPreferredFoods() {
        return preferredFoods;
    }
    
    public void setPreferredFoods(String preferredFoods) {
        this.preferredFoods = preferredFoods;
    }
    
    public String getDislikedFoods() {
        return dislikedFoods;
    }
    
    public void setDislikedFoods(String dislikedFoods) {
        this.dislikedFoods = dislikedFoods;
    }
    
    public String getAllergies() {
        return allergies;
    }
    
    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }
    
    public String getDietaryRestriction() {
        return dietaryRestriction;
    }
    
    public void setDietaryRestriction(String dietaryRestriction) {
        this.dietaryRestriction = dietaryRestriction;
    }
    
    public Integer getCookingTimePreference() {
        return cookingTimePreference;
    }
    
    public void setCookingTimePreference(Integer cookingTimePreference) {
        this.cookingTimePreference = cookingTimePreference;
    }
    
    public Integer getBudgetPerDay() {
        return budgetPerDay;
    }
    
    public void setBudgetPerDay(Integer budgetPerDay) {
        this.budgetPerDay = budgetPerDay;
    }
    
    public String getFavoriteCuisines() {
        return favoriteCuisines;
    }
    
    public void setFavoriteCuisines(String favoriteCuisines) {
        this.favoriteCuisines = favoriteCuisines;
    }
}













