package com.aifitness.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic meal plan document exposed to the frontend.
 * Ensures meal outputs remain JSON-safe and self-contained.
 */
public class RuleBasedMealPlanDocument {

    private Integer dailyCalories;
    private DailyMacrosDTO macros;
    private List<MealItem> meals = new ArrayList<>();
    private List<String> shoppingList = new ArrayList<>();

    public Integer getDailyCalories() {
        return dailyCalories;
    }

    public void setDailyCalories(Integer dailyCalories) {
        this.dailyCalories = dailyCalories;
    }

    public DailyMacrosDTO getMacros() {
        return macros;
    }

    public void setMacros(DailyMacrosDTO macros) {
        this.macros = macros;
    }

    public List<MealItem> getMeals() {
        return meals;
    }

    public void setMeals(List<MealItem> meals) {
        this.meals = meals;
    }

    public List<String> getShoppingList() {
        return shoppingList;
    }

    public void setShoppingList(List<String> shoppingList) {
        this.shoppingList = shoppingList;
    }

    /**
     * Lightweight representation of each meal entry.
     */
    public static class MealItem {
        private LocalDate date;
        private String mealType;
        private String name;
        private Integer calories;
        private Integer protein;
        private Integer carbs;
        private Integer fats;

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
    }
}


