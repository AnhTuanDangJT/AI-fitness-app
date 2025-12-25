package com.aifitness.repository;

import com.aifitness.entity.DailyCheckIn;
import com.aifitness.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Daily Check-In Repository
 * 
 * Provides data access methods for DailyCheckIn entity.
 */
@Repository
public interface DailyCheckInRepository extends JpaRepository<DailyCheckIn, Long> {
    
    /**
     * Finds check-in by user and date.
     * 
     * Used to check if a check-in already exists for a specific date.
     */
    Optional<DailyCheckIn> findByUserAndDate(User user, LocalDate date);
    
    /**
     * Finds all check-ins for a user, ordered by date descending (most recent first).
     */
    List<DailyCheckIn> findByUserOrderByDateDesc(User user);
    
    /**
     * Finds check-ins for a user within a date range, ordered by date ascending.
     */
    List<DailyCheckIn> findByUserAndDateBetweenOrderByDateAsc(
            User user, LocalDate startDate, LocalDate endDate);
}










