package com.aifitness.repository;

import com.aifitness.entity.User;
import com.aifitness.entity.WeeklyProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Weekly Progress Repository
 * 
 * Provides data access methods for WeeklyProgress entity.
 * 
 * Spring Data JPA will automatically implement these methods based on naming conventions.
 */
@Repository
public interface WeeklyProgressRepository extends JpaRepository<WeeklyProgress, Long> {
    
    /**
     * Finds weekly progress entry by user and week start date.
     * 
     * Used to check if a progress entry already exists for a specific week.
     */
    Optional<WeeklyProgress> findByUserAndWeekStartDate(User user, LocalDate weekStartDate);
    
    /**
     * Finds all weekly progress entries for a user, ordered by week start date descending.
     * 
     * Used to retrieve recent progress entries.
     */
    List<WeeklyProgress> findByUserOrderByWeekStartDateDesc(User user);
    
    
    /**
     * Checks if a progress entry exists for a user and week start date.
     */
    boolean existsByUserAndWeekStartDate(User user, LocalDate weekStartDate);
    
    /**
     * Counts the number of progress entries for a user.
     */
    long countByUser(User user);
}

