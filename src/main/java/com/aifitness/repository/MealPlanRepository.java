package com.aifitness.repository;

import com.aifitness.entity.MealPlan;
import com.aifitness.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Meal Plan Repository
 * 
 * Provides data access methods for MealPlan entity.
 */
@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {
    
    /**
     * Finds meal plan by user and week start date.
     * 
     * Used to check if a meal plan already exists for a specific week.
     */
    Optional<MealPlan> findByUserAndWeekStartDate(User user, LocalDate weekStartDate);
    
    /**
     * Finds all meal plans for a user, ordered by week start date descending.
     * 
     * Used to retrieve recent meal plans.
     */
    List<MealPlan> findByUserOrderByWeekStartDateDesc(User user);
    
    /**
     * Finds the latest meal plan for a user.
     * 
     * Returns the most recent meal plan based on week start date.
     */
    Optional<MealPlan> findFirstByUserOrderByWeekStartDateDesc(User user);
    
    /**
     * Checks if a meal plan exists for a user and week start date.
     */
    boolean existsByUserAndWeekStartDate(User user, LocalDate weekStartDate);
    
    /**
     * Counts the number of meal plans for a user.
     */
    long countByUser(User user);
}





