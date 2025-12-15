package com.aifitness.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI Context Response DTO
 * 
 * Response object for GET /api/ai/context endpoint.
 * Contains all relevant user data for AI Coach to use.
 */
public class AiContextResponse {
    
    private UserContext user;
    private NutritionTargets nutritionTargets;
    private BodyAnalysisLatest bodyAnalysisLatest;
    private WeeklyProgressLatest weeklyProgressLatest;
    private MealPlanLatest mealPlanLatest;
    private MealPreferences mealPreferences;
    private Gamification gamification;
    
    // Nested classes for structured response
    
    public static class UserContext {
        private Long id;
        private String name;
        private Integer goal; // 1=Lose weight, 2=Maintain, 3=Gain muscle, 4=Recomposition
        private Boolean gender; // true = male, false = female
        private Double height;
        private Double weight;
        private Integer age;
        private Integer activityLevel;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getGoal() { return goal; }
        public void setGoal(Integer goal) { this.goal = goal; }
        public Boolean getGender() { return gender; }
        public void setGender(Boolean gender) { this.gender = gender; }
        public Double getHeight() { return height; }
        public void setHeight(Double height) { this.height = height; }
        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public Integer getActivityLevel() { return activityLevel; }
        public void setActivityLevel(Integer activityLevel) { this.activityLevel = activityLevel; }
    }
    
    public static class NutritionTargets {
        private Double calories;
        private Double protein;
        private Double carbs;
        private Double fat;
        
        // Getters and setters
        public Double getCalories() { return calories; }
        public void setCalories(Double calories) { this.calories = calories; }
        public Double getProtein() { return protein; }
        public void setProtein(Double protein) { this.protein = protein; }
        public Double getCarbs() { return carbs; }
        public void setCarbs(Double carbs) { this.carbs = carbs; }
        public Double getFat() { return fat; }
        public void setFat(Double fat) { this.fat = fat; }
    }
    
    public static class BodyAnalysisLatest {
        private Double bmi;
        private Double whr;
        private Double bmr;
        private Double tdee;
        private Double bodyFatPct;
        private LocalDateTime updatedAt;
        
        // Getters and setters
        public Double getBmi() { return bmi; }
        public void setBmi(Double bmi) { this.bmi = bmi; }
        public Double getWhr() { return whr; }
        public void setWhr(Double whr) { this.whr = whr; }
        public Double getBmr() { return bmr; }
        public void setBmr(Double bmr) { this.bmr = bmr; }
        public Double getTdee() { return tdee; }
        public void setTdee(Double tdee) { this.tdee = tdee; }
        public Double getBodyFatPct() { return bodyFatPct; }
        public void setBodyFatPct(Double bodyFatPct) { this.bodyFatPct = bodyFatPct; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
    
    public static class WeeklyProgressLatest {
        private Double weight;
        private String notes;
        private LocalDate createdAt;
        
        // Getters and setters
        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public LocalDate getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    }
    
    public static class MealPlanLatest {
        private LocalDate weekStart;
        private String summary;
        
        // Getters and setters
        public LocalDate getWeekStart() { return weekStart; }
        public void setWeekStart(LocalDate weekStart) { this.weekStart = weekStart; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
    }
    
    public static class MealPreferences {
        private String preferredFoods;
        private String dislikedFoods;
        private String cuisines;
        private String allergies;
        private Integer budget;
        private Integer cookTime;
        
        // Getters and setters
        public String getPreferredFoods() { return preferredFoods; }
        public void setPreferredFoods(String preferredFoods) { this.preferredFoods = preferredFoods; }
        public String getDislikedFoods() { return dislikedFoods; }
        public void setDislikedFoods(String dislikedFoods) { this.dislikedFoods = dislikedFoods; }
        public String getCuisines() { return cuisines; }
        public void setCuisines(String cuisines) { this.cuisines = cuisines; }
        public String getAllergies() { return allergies; }
        public void setAllergies(String allergies) { this.allergies = allergies; }
        public Integer getBudget() { return budget; }
        public void setBudget(Integer budget) { this.budget = budget; }
        public Integer getCookTime() { return cookTime; }
        public void setCookTime(Integer cookTime) { this.cookTime = cookTime; }
    }
    
    public static class Gamification {
        private Integer xp;
        private Integer currentStreakDays;
        private Integer longestStreakDays;
        private List<String> badges;
        
        // Getters and setters
        public Integer getXp() { return xp; }
        public void setXp(Integer xp) { this.xp = xp; }
        public Integer getCurrentStreakDays() { return currentStreakDays; }
        public void setCurrentStreakDays(Integer currentStreakDays) { this.currentStreakDays = currentStreakDays; }
        public Integer getLongestStreakDays() { return longestStreakDays; }
        public void setLongestStreakDays(Integer longestStreakDays) { this.longestStreakDays = longestStreakDays; }
        public List<String> getBadges() { return badges; }
        public void setBadges(List<String> badges) { this.badges = badges; }
    }
    
    // Main class getters and setters
    public UserContext getUser() { return user; }
    public void setUser(UserContext user) { this.user = user; }
    public NutritionTargets getNutritionTargets() { return nutritionTargets; }
    public void setNutritionTargets(NutritionTargets nutritionTargets) { this.nutritionTargets = nutritionTargets; }
    public BodyAnalysisLatest getBodyAnalysisLatest() { return bodyAnalysisLatest; }
    public void setBodyAnalysisLatest(BodyAnalysisLatest bodyAnalysisLatest) { this.bodyAnalysisLatest = bodyAnalysisLatest; }
    public WeeklyProgressLatest getWeeklyProgressLatest() { return weeklyProgressLatest; }
    public void setWeeklyProgressLatest(WeeklyProgressLatest weeklyProgressLatest) { this.weeklyProgressLatest = weeklyProgressLatest; }
    public MealPlanLatest getMealPlanLatest() { return mealPlanLatest; }
    public void setMealPlanLatest(MealPlanLatest mealPlanLatest) { this.mealPlanLatest = mealPlanLatest; }
    public MealPreferences getMealPreferences() { return mealPreferences; }
    public void setMealPreferences(MealPreferences mealPreferences) { this.mealPreferences = mealPreferences; }
    public Gamification getGamification() { return gamification; }
    public void setGamification(Gamification gamification) { this.gamification = gamification; }
}

