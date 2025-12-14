package com.aifitness.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Coach Context DTO
 * 
 * Aggregated context data for AI Coach to use when generating responses.
 * This includes user profile, daily check-ins, meal plan, etc.
 */
public class CoachContext {
    
    // User profile metrics
    private Double bmi;
    private Double tdee;
    private Double goalCalories;
    private Double proteinTarget;
    private String goal; // "lose_weight", "maintain", "gain_muscle", "recomposition"
    private Integer activityLevel;
    
    // Today's date
    private LocalDate today;
    
    // Last N days summary (day-by-day format)
    private List<DailyCheckInResponse> recentCheckIns;
    
    // Latest meal plan if available
    private Map<String, Object> mealPlan; // Simplified meal plan data
    
    // Weekly progress summary (if available)
    private List<WeeklyProgressResponse> recentWeeklyProgress;
    
    // Constructors
    
    public CoachContext() {
    }
    
    // Getters and Setters
    
    public Double getBmi() {
        return bmi;
    }
    
    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }
    
    public Double getTdee() {
        return tdee;
    }
    
    public void setTdee(Double tdee) {
        this.tdee = tdee;
    }
    
    public Double getGoalCalories() {
        return goalCalories;
    }
    
    public void setGoalCalories(Double goalCalories) {
        this.goalCalories = goalCalories;
    }
    
    public Double getProteinTarget() {
        return proteinTarget;
    }
    
    public void setProteinTarget(Double proteinTarget) {
        this.proteinTarget = proteinTarget;
    }
    
    public String getGoal() {
        return goal;
    }
    
    public void setGoal(String goal) {
        this.goal = goal;
    }
    
    public Integer getActivityLevel() {
        return activityLevel;
    }
    
    public void setActivityLevel(Integer activityLevel) {
        this.activityLevel = activityLevel;
    }
    
    public LocalDate getToday() {
        return today;
    }
    
    public void setToday(LocalDate today) {
        this.today = today;
    }
    
    public List<DailyCheckInResponse> getRecentCheckIns() {
        return recentCheckIns;
    }
    
    public void setRecentCheckIns(List<DailyCheckInResponse> recentCheckIns) {
        this.recentCheckIns = recentCheckIns;
    }
    
    public Map<String, Object> getMealPlan() {
        return mealPlan;
    }
    
    public void setMealPlan(Map<String, Object> mealPlan) {
        this.mealPlan = mealPlan;
    }
    
    public List<WeeklyProgressResponse> getRecentWeeklyProgress() {
        return recentWeeklyProgress;
    }
    
    public void setRecentWeeklyProgress(List<WeeklyProgressResponse> recentWeeklyProgress) {
        this.recentWeeklyProgress = recentWeeklyProgress;
    }
}


