package com.aifitness.service;

import com.aifitness.dto.DailyCheckInRequest;
import com.aifitness.dto.DailyCheckInResponse;
import com.aifitness.entity.DailyCheckIn;
import com.aifitness.entity.User;
import com.aifitness.repository.DailyCheckInRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Daily Check-In Service
 * 
 * Handles daily check-in log operations.
 * This service manages the data that will be used by the AI coach for day-by-day analysis.
 */
@Service
@Transactional
public class DailyCheckInService {
    
    private final DailyCheckInRepository dailyCheckInRepository;
    
    @Autowired
    public DailyCheckInService(DailyCheckInRepository dailyCheckInRepository) {
        this.dailyCheckInRepository = dailyCheckInRepository;
    }
    
    /**
     * Saves or updates a daily check-in entry for a user.
     * 
     * If a check-in already exists for the same date, it will be updated.
     * Otherwise, a new entry will be created.
     * 
     * @param user The user who owns this check-in entry
     * @param request The check-in data to save
     * @return The saved/updated daily check-in response
     */
    public DailyCheckInResponse saveDailyCheckIn(User user, DailyCheckInRequest request) {
        // Check if an entry already exists for this date
        DailyCheckIn checkIn = dailyCheckInRepository
                .findByUserAndDate(user, request.getDate())
                .orElse(null);
        
        if (checkIn == null) {
            // Create new entry
            checkIn = new DailyCheckIn(user, request.getDate());
        }
        // Otherwise, update existing entry
        
        // Update all fields (only if provided)
        if (request.getWeight() != null) {
            checkIn.setWeight(request.getWeight());
        }
        if (request.getSteps() != null) {
            checkIn.setSteps(request.getSteps());
        }
        if (request.getWorkoutDone() != null) {
            checkIn.setWorkoutDone(request.getWorkoutDone());
        }
        if (request.getNotes() != null) {
            checkIn.setNotes(request.getNotes());
        }
        
        // Save to database
        checkIn = dailyCheckInRepository.save(checkIn);
        
        // Convert to response DTO
        return convertToResponse(checkIn);
    }
    
    /**
     * Gets daily check-ins for a user within a date range.
     * 
     * @param user The user
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of daily check-in responses, ordered by date ascending
     */
    public List<DailyCheckInResponse> getCheckInsForDateRange(User user, LocalDate startDate, LocalDate endDate) {
        List<DailyCheckIn> checkIns = dailyCheckInRepository
                .findByUserAndDateBetweenOrderByDateAsc(user, startDate, endDate);
        
        return checkIns.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets recent daily check-ins for a user.
     * 
     * @param user The user
     * @param lastNDays Number of days to retrieve (default: 7)
     * @return List of daily check-in responses, ordered by date descending
     */
    public List<DailyCheckInResponse> getRecentCheckIns(User user, int lastNDays) {
        // Ensure lastNDays is reasonable (between 1 and 365)
        int days = Math.max(1, Math.min(365, lastNDays));
        
        // Get recent entries
        List<DailyCheckIn> checkIns = dailyCheckInRepository
                .findByUserOrderByDateDesc(user);
        
        // Limit to lastNDays
        List<DailyCheckIn> limitedList = checkIns.stream()
                .limit(days)
                .collect(Collectors.toList());
        
        // Convert to response DTOs
        return limitedList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Converts DailyCheckIn entity to DailyCheckInResponse DTO.
     */
    private DailyCheckInResponse convertToResponse(DailyCheckIn checkIn) {
        return new DailyCheckInResponse(
                checkIn.getId(),
                checkIn.getDate(),
                checkIn.getWeight(),
                checkIn.getSteps(),
                checkIn.getWorkoutDone(),
                checkIn.getNotes(),
                checkIn.getCreatedAt(),
                checkIn.getUpdatedAt()
        );
    }
}











