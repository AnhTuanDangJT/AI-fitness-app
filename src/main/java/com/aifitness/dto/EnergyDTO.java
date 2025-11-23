package com.aifitness.dto;

/**
 * Energy DTO
 */
public class EnergyDTO {
    private Double bmr;
    private Double tdee;
    private Double goalCalories;
    
    public EnergyDTO() {
    }
    
    // Getters and Setters
    public Double getBmr() {
        return bmr;
    }
    
    public void setBmr(Double bmr) {
        this.bmr = bmr;
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
}

