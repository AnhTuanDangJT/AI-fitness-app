package com.aifitness.dto;

/**
 * Full Analysis Response DTO
 * 
 * Response for /profile/full-analysis endpoint.
 * Combines all health calculations into a single response.
 */
public class FullAnalysisResponse {
    private double bmi;
    private String bmiCategory;
    private double whr;
    private String whrRisk;
    private double bmr;
    private double tdee;
    private double goalCalories;
    private double proteinTarget;
    
    public FullAnalysisResponse() {
    }
    
    public FullAnalysisResponse(double bmi, String bmiCategory, double whr, String whrRisk,
                               double bmr, double tdee, double goalCalories, double proteinTarget) {
        this.bmi = bmi;
        this.bmiCategory = bmiCategory;
        this.whr = whr;
        this.whrRisk = whrRisk;
        this.bmr = bmr;
        this.tdee = tdee;
        this.goalCalories = goalCalories;
        this.proteinTarget = proteinTarget;
    }
    
    // Getters and Setters
    
    public double getBmi() {
        return bmi;
    }
    
    public void setBmi(double bmi) {
        this.bmi = bmi;
    }
    
    public String getBmiCategory() {
        return bmiCategory;
    }
    
    public void setBmiCategory(String bmiCategory) {
        this.bmiCategory = bmiCategory;
    }
    
    public double getWhr() {
        return whr;
    }
    
    public void setWhr(double whr) {
        this.whr = whr;
    }
    
    public String getWhrRisk() {
        return whrRisk;
    }
    
    public void setWhrRisk(String whrRisk) {
        this.whrRisk = whrRisk;
    }
    
    public double getBmr() {
        return bmr;
    }
    
    public void setBmr(double bmr) {
        this.bmr = bmr;
    }
    
    public double getTdee() {
        return tdee;
    }
    
    public void setTdee(double tdee) {
        this.tdee = tdee;
    }
    
    public double getGoalCalories() {
        return goalCalories;
    }
    
    public void setGoalCalories(double goalCalories) {
        this.goalCalories = goalCalories;
    }
    
    public double getProteinTarget() {
        return proteinTarget;
    }
    
    public void setProteinTarget(double proteinTarget) {
        this.proteinTarget = proteinTarget;
    }
}

