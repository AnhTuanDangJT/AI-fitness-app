package com.aifitness.dto;

/**
 * WHR Calculation Response DTO
 * 
 * Response for /calculate/whr endpoint.
 */
public class WHRCalculationResponse {
    private double whr;
    private String riskLevel;
    
    public WHRCalculationResponse() {
    }
    
    public WHRCalculationResponse(double whr, String riskLevel) {
        this.whr = whr;
        this.riskLevel = riskLevel;
    }
    
    public double getWhr() {
        return whr;
    }
    
    public void setWhr(double whr) {
        this.whr = whr;
    }
    
    public String getRiskLevel() {
        return riskLevel;
    }
    
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
}

