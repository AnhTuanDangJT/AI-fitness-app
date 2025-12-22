package com.aifitness.service;

import com.aifitness.dto.WeeklyProgressRequest;
import com.aifitness.dto.WeeklyProgressResponse;
import com.aifitness.entity.User;
import com.aifitness.entity.WeeklyProgress;
import com.aifitness.repository.WeeklyProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Weekly Progress Service
 * 
 * Handles weekly progress log operations.
 * This service manages the data that will be used by the AI coach for analysis.
 */
@Service
@Transactional
public class WeeklyProgressService {
    
    private final WeeklyProgressRepository weeklyProgressRepository;
    
    @Autowired
    public WeeklyProgressService(WeeklyProgressRepository weeklyProgressRepository) {
        this.weeklyProgressRepository = weeklyProgressRepository;
    }
    
    /**
     * Saves or updates a weekly progress entry for a user.
     * 
     * If a progress entry already exists for the same week, it will be updated.
     * Otherwise, a new entry will be created.
     * 
     * @param user The user who owns this progress entry
     * @param request The progress data to save
     * @return The saved/updated weekly progress response
     */
    public WeeklyProgressResponse saveWeeklyProgress(User user, WeeklyProgressRequest request) {
        // Check if an entry already exists for this week
        WeeklyProgress progress = weeklyProgressRepository
                .findByUserAndWeekStartDate(user, request.getWeekStartDate())
                .orElse(null);
        
        if (progress == null) {
            // Create new entry
            progress = new WeeklyProgress(user, request.getWeekStartDate());
        }
        // Otherwise, update existing entry
        
        // Update all fields
        progress.setWeight(request.getWeight());
        progress.setSleepHoursPerNightAverage(request.getSleepHoursPerNightAverage());
        progress.setStressLevel(request.getStressLevel());
        progress.setHungerLevel(request.getHungerLevel());
        progress.setEnergyLevel(request.getEnergyLevel());
        progress.setTrainingSessionsCompleted(request.getTrainingSessionsCompleted());
        progress.setCaloriesAverage(request.getCaloriesAverage());
        
        // Save to database
        progress = weeklyProgressRepository.save(progress);
        
        // Convert to response DTO
        return convertToResponse(progress);
    }
    
    /**
     * Gets recent weekly progress entries for a user.
     * 
     * @param user The user
     * @param lastNWeeks Number of weeks to retrieve (default: 8)
     * @return List of weekly progress responses, ordered by week start date descending
     */
    public List<WeeklyProgressResponse> getRecentProgressForUser(User user, int lastNWeeks) {
        // Ensure lastNWeeks is reasonable (between 1 and 52)
        int weeks = Math.max(1, Math.min(52, lastNWeeks));
        
        // Get recent entries
        List<WeeklyProgress> progressList = weeklyProgressRepository
                .findByUserOrderByWeekStartDateDesc(user);
        
        // Limit to lastNWeeks
        List<WeeklyProgress> limitedList = progressList.stream()
                .limit(weeks)
                .collect(Collectors.toList());
        
        // Convert to response DTOs
        return limitedList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Converts WeeklyProgress entity to WeeklyProgressResponse DTO.
     */
    private WeeklyProgressResponse convertToResponse(WeeklyProgress progress) {
        return new WeeklyProgressResponse(
                progress.getId(),
                progress.getWeekStartDate(),
                progress.getWeight(),
                progress.getSleepHoursPerNightAverage(),
                progress.getStressLevel(),
                progress.getHungerLevel(),
                progress.getEnergyLevel(),
                progress.getTrainingSessionsCompleted(),
                progress.getCaloriesAverage(),
                progress.getCreatedAt(),
                progress.getUpdatedAt()
        );
    }
}











