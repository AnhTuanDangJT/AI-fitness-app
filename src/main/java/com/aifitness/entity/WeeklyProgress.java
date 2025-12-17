package com.aifitness.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Weekly Progress Entity
 * 
 * Stores weekly progress logs for users to track fitness metrics over time.
 * This data will be used by the AI coach to analyze trends and provide insights.
 */
@Entity
@Table(name = "weekly_progress")
public class WeeklyProgress {
    
    /**
     * Primary Key - Auto-generated ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * User who owns this progress entry
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Start date of the week (typically Monday)
     * Used to uniquely identify a week for a user
     */
    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;
    
    /**
     * Average weight for the week (in kg)
     */
    @Column(name = "weight")
    private Double weight;
    
    /**
     * Average sleep hours per night for the week
     * Range: 0-24 hours
     */
    @Column(name = "sleep_hours_per_night_average")
    private Integer sleepHoursPerNightAverage;
    
    /**
     * Average stress level for the week
     * Range: 1-10 (1 = very low, 10 = very high)
     */
    @Column(name = "stress_level")
    private Integer stressLevel;
    
    /**
     * Average hunger level for the week
     * Range: 1-10 (1 = not hungry, 10 = very hungry)
     */
    @Column(name = "hunger_level")
    private Integer hungerLevel;
    
    /**
     * Average energy level for the week
     * Range: 1-10 (1 = very low, 10 = very high)
     */
    @Column(name = "energy_level")
    private Integer energyLevel;
    
    /**
     * Number of training sessions completed during the week
     */
    @Column(name = "training_sessions_completed")
    private Integer trainingSessionsCompleted;
    
    /**
     * Average daily calories consumed for the week
     */
    @Column(name = "calories_average")
    private Double caloriesAverage;
    
    /**
     * Created Timestamp - Automatically set on creation
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Updated Timestamp - Automatically set on update
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor - Required by JPA
     */
    public WeeklyProgress() {
    }
    
    /**
     * Constructor for creating a new weekly progress entry
     */
    public WeeklyProgress(User user, LocalDate weekStartDate) {
        this.user = user;
        this.weekStartDate = weekStartDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * JPA Lifecycle Callback - Set createdAt before persisting
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * JPA Lifecycle Callback - Set updatedAt before updating
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }
    
    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }
    
    public Double getWeight() {
        return weight;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    public Integer getSleepHoursPerNightAverage() {
        return sleepHoursPerNightAverage;
    }
    
    public void setSleepHoursPerNightAverage(Integer sleepHoursPerNightAverage) {
        this.sleepHoursPerNightAverage = sleepHoursPerNightAverage;
    }
    
    public Integer getStressLevel() {
        return stressLevel;
    }
    
    public void setStressLevel(Integer stressLevel) {
        this.stressLevel = stressLevel;
    }
    
    public Integer getHungerLevel() {
        return hungerLevel;
    }
    
    public void setHungerLevel(Integer hungerLevel) {
        this.hungerLevel = hungerLevel;
    }
    
    public Integer getEnergyLevel() {
        return energyLevel;
    }
    
    public void setEnergyLevel(Integer energyLevel) {
        this.energyLevel = energyLevel;
    }
    
    public Integer getTrainingSessionsCompleted() {
        return trainingSessionsCompleted;
    }
    
    public void setTrainingSessionsCompleted(Integer trainingSessionsCompleted) {
        this.trainingSessionsCompleted = trainingSessionsCompleted;
    }
    
    public Double getCaloriesAverage() {
        return caloriesAverage;
    }
    
    public void setCaloriesAverage(Double caloriesAverage) {
        this.caloriesAverage = caloriesAverage;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "WeeklyProgress{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", weekStartDate=" + weekStartDate +
                ", weight=" + weight +
                ", sleepHoursPerNightAverage=" + sleepHoursPerNightAverage +
                ", stressLevel=" + stressLevel +
                ", hungerLevel=" + hungerLevel +
                ", energyLevel=" + energyLevel +
                ", trainingSessionsCompleted=" + trainingSessionsCompleted +
                ", caloriesAverage=" + caloriesAverage +
                ", createdAt=" + createdAt +
                '}';
    }
}






