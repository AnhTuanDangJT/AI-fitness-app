package com.aifitness.dto;

/**
 * Grocery Item DTO
 * 
 * Represents a single item in a grocery list.
 */
public class GroceryItem {
    
    private String name;
    private String quantityText;
    private Boolean alreadyHave;
    
    // Constructors
    public GroceryItem() {
        this.alreadyHave = false;
    }
    
    public GroceryItem(String name, String quantityText) {
        this.name = name;
        this.quantityText = quantityText;
        this.alreadyHave = false;
    }
    
    public GroceryItem(String name, String quantityText, Boolean alreadyHave) {
        this.name = name;
        this.quantityText = quantityText;
        this.alreadyHave = alreadyHave != null ? alreadyHave : false;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getQuantityText() {
        return quantityText;
    }
    
    public void setQuantityText(String quantityText) {
        this.quantityText = quantityText;
    }
    
    public Boolean getAlreadyHave() {
        return alreadyHave;
    }
    
    public void setAlreadyHave(Boolean alreadyHave) {
        this.alreadyHave = alreadyHave != null ? alreadyHave : false;
    }
    
    @Override
    public String toString() {
        return "GroceryItem{" +
                "name='" + name + '\'' +
                ", quantityText='" + quantityText + '\'' +
                ", alreadyHave=" + alreadyHave +
                '}';
    }
}












