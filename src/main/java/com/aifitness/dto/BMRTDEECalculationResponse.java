package com.aifitness.dto;

/**
 * BMR and TDEE Calculation Response DTO
 * 
 * Response for /calculate/bmr-tdee endpoint.
 */
public class BMRTDEECalculationResponse {
    private double bmr;
    private double tdee;
    private String activityLevelDescription;
    
    public BMRTDEECalculationResponse() {
    }
    
    public BMRTDEECalculationResponse(double bmr, double tdee, String activityLevelDescription) {
        this.bmr = bmr;
        this.tdee = tdee;
        this.activityLevelDescription = activityLevelDescription;
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
    
    public String getActivityLevelDescription() {
        return activityLevelDescription;
    }
    
    public void setActivityLevelDescription(String activityLevelDescription) {
        this.activityLevelDescription = activityLevelDescription;
    }
}

