package com.aifitness.repository;

import com.aifitness.entity.MealPlan;
import com.aifitness.entity.MealPlanEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Meal Plan Entry Repository
 * 
 * Provides data access methods for MealPlanEntry entity.
 */
@Repository
public interface MealPlanEntryRepository extends JpaRepository<MealPlanEntry, Long> {
    
    /**
     * Finds all entries for a meal plan, ordered by date and meal type.
     */
    List<MealPlanEntry> findByMealPlanOrderByDateAscMealTypeAsc(MealPlan mealPlan);
    
    /**
     * Finds all entries for a meal plan on a specific date.
     */
    List<MealPlanEntry> findByMealPlanAndDate(MealPlan mealPlan, LocalDate date);
    
    /**
     * Finds all entries for a meal plan of a specific meal type.
     */
    List<MealPlanEntry> findByMealPlanAndMealType(MealPlan mealPlan, String mealType);
}











