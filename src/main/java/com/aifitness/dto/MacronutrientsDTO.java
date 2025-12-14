package com.aifitness.dto;

/**
 * Macronutrients DTO
 */
public class MacronutrientsDTO {
    private Double protein;
    private Double fat;
    private Double carbohydrates;
    private Integer fiber;
    private Double water;
    
    public MacronutrientsDTO() {
    }
    
    // Getters and Setters
    public Double getProtein() {
        return protein;
    }
    
    public void setProtein(Double protein) {
        this.protein = protein;
    }
    
    public Double getFat() {
        return fat;
    }
    
    public void setFat(Double fat) {
        this.fat = fat;
    }
    
    public Double getCarbohydrates() {
        return carbohydrates;
    }
    
    public void setCarbohydrates(Double carbohydrates) {
        this.carbohydrates = carbohydrates;
    }
    
    public Integer getFiber() {
        return fiber;
    }
    
    public void setFiber(Integer fiber) {
        this.fiber = fiber;
    }
    
    public Double getWater() {
        return water;
    }
    
    public void setWater(Double water) {
        this.water = water;
    }
}

