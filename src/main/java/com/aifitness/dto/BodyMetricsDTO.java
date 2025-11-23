package com.aifitness.dto;

/**
 * Body Metrics DTO
 */
public class BodyMetricsDTO {
    private Double bmi;
    private String bmiCategory;
    private Double whr;
    private String whrHealthStatus;
    private Double whtr;
    private String whtrRiskLevel;
    private Double bodyFat;
    
    public BodyMetricsDTO() {
    }
    
    // Getters and Setters
    public Double getBmi() {
        return bmi;
    }
    
    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }
    
    public String getBmiCategory() {
        return bmiCategory;
    }
    
    public void setBmiCategory(String bmiCategory) {
        this.bmiCategory = bmiCategory;
    }
    
    public Double getWhr() {
        return whr;
    }
    
    public void setWhr(Double whr) {
        this.whr = whr;
    }
    
    public String getWhrHealthStatus() {
        return whrHealthStatus;
    }
    
    public void setWhrHealthStatus(String whrHealthStatus) {
        this.whrHealthStatus = whrHealthStatus;
    }
    
    public Double getWhtr() {
        return whtr;
    }
    
    public void setWhtr(Double whtr) {
        this.whtr = whtr;
    }
    
    public String getWhtrRiskLevel() {
        return whtrRiskLevel;
    }
    
    public void setWhtrRiskLevel(String whtrRiskLevel) {
        this.whtrRiskLevel = whtrRiskLevel;
    }
    
    public Double getBodyFat() {
        return bodyFat;
    }
    
    public void setBodyFat(Double bodyFat) {
        this.bodyFat = bodyFat;
    }
}

