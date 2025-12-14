package com.aifitness.dto;

/**
 * Nutrition Goal Calculation Response DTO
 * 
 * Response for /calculate/nutrition-goal endpoint.
 */
public class NutritionGoalCalculationResponse {
    private double calorieTarget;
    private double proteinTarget;
    private String goalDescription;
    
    public NutritionGoalCalculationResponse() {
    }
    
    public NutritionGoalCalculationResponse(double calorieTarget, double proteinTarget, String goalDescription) {
        this.calorieTarget = calorieTarget;
        this.proteinTarget = proteinTarget;
        this.goalDescription = goalDescription;
    }
    
    public double getCalorieTarget() {
        return calorieTarget;
    }
    
    public void setCalorieTarget(double calorieTarget) {
        this.calorieTarget = calorieTarget;
    }
    
    public double getProteinTarget() {
        return proteinTarget;
    }
    
    public void setProteinTarget(double proteinTarget) {
        this.proteinTarget = proteinTarget;
    }
    
    public String getGoalDescription() {
        return goalDescription;
    }
    
    public void setGoalDescription(String goalDescription) {
        this.goalDescription = goalDescription;
    }
}

