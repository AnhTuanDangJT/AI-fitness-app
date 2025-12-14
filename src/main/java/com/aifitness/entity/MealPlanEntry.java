package com.aifitness.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Meal Plan Entry Entity
 * 
 * Represents a single meal within a meal plan.
 * Each entry corresponds to one meal (breakfast, lunch, dinner, or snack) on a specific date.
 */
@Entity
@Table(name = "meal_plan_entries", indexes = {
    @Index(name = "idx_meal_plan_date", columnList = "meal_plan_id, date"),
    @Index(name = "idx_meal_plan_type", columnList = "meal_plan_id, meal_type")
})
public class MealPlanEntry {
    
    /**
     * Meal type constants
     */
    public static final String BREAKFAST = "BREAKFAST";
    public static final String LUNCH = "LUNCH";
    public static final String DINNER = "DINNER";
    public static final String SNACK = "SNACK";
    
    /**
     * Primary Key - Auto-generated ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * Meal plan this entry belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan mealPlan;
    
    /**
     * Date of the meal
     */
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    /**
     * Meal type: BREAKFAST, LUNCH, DINNER, or SNACK
     */
    @Column(name = "meal_type", nullable = false, length = 20)
    private String mealType;
    
    /**
     * Name/description of the meal
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    /**
     * Calories in the meal
     */
    @Column(name = "calories", nullable = false)
    private Integer calories;
    
    /**
     * Protein in grams
     */
    @Column(name = "protein", nullable = false)
    private Integer protein;
    
    /**
     * Carbohydrates in grams
     */
    @Column(name = "carbs", nullable = false)
    private Integer carbs;
    
    /**
     * Fats in grams
     */
    @Column(name = "fats", nullable = false)
    private Integer fats;
    
    /**
     * Ingredients as JSON string (stored as TEXT in database)
     * Format: [{"name":"chicken breast","quantityText":"200g"},...]
     */
    @Column(name = "ingredients", columnDefinition = "TEXT")
    private String ingredients;
    
    /**
     * Default constructor - Required by JPA
     */
    public MealPlanEntry() {
    }
    
    /**
     * Constructor for creating a new meal plan entry
     */
    public MealPlanEntry(MealPlan mealPlan, LocalDate date, String mealType, String name,
                        Integer calories, Integer protein, Integer carbs, Integer fats) {
        this.mealPlan = mealPlan;
        this.date = date;
        this.mealType = mealType;
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
    }
    
    /**
     * Constructor with ingredients
     */
    public MealPlanEntry(MealPlan mealPlan, LocalDate date, String mealType, String name,
                        Integer calories, Integer protein, Integer carbs, Integer fats, String ingredients) {
        this.mealPlan = mealPlan;
        this.date = date;
        this.mealType = mealType;
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
        this.ingredients = ingredients;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public MealPlan getMealPlan() {
        return mealPlan;
    }
    
    public void setMealPlan(MealPlan mealPlan) {
        this.mealPlan = mealPlan;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public String getMealType() {
        return mealType;
    }
    
    public void setMealType(String mealType) {
        this.mealType = mealType;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getCalories() {
        return calories;
    }
    
    public void setCalories(Integer calories) {
        this.calories = calories;
    }
    
    public Integer getProtein() {
        return protein;
    }
    
    public void setProtein(Integer protein) {
        this.protein = protein;
    }
    
    public Integer getCarbs() {
        return carbs;
    }
    
    public void setCarbs(Integer carbs) {
        this.carbs = carbs;
    }
    
    public Integer getFats() {
        return fats;
    }
    
    public void setFats(Integer fats) {
        this.fats = fats;
    }
    
    public String getIngredients() {
        return ingredients;
    }
    
    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }
    
    @Override
    public String toString() {
        return "MealPlanEntry{" +
                "id=" + id +
                ", date=" + date +
                ", mealType='" + mealType + '\'' +
                ", name='" + name + '\'' +
                ", calories=" + calories +
                ", protein=" + protein +
                ", carbs=" + carbs +
                ", fats=" + fats +
                '}';
    }
}

