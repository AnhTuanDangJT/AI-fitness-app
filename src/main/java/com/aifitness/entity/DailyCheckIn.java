package com.aifitness.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Daily Check-In Entity
 * 
 * Stores daily check-in logs for users to track daily metrics.
 * This data will be used by the AI coach for day-by-day coaching.
 */
@Entity
@Table(name = "daily_checkins", indexes = {
    @Index(name = "idx_user_date", columnList = "user_id, date", unique = true),
    @Index(name = "idx_user_created", columnList = "user_id, created_at")
})
public class DailyCheckIn {
    
    /**
     * Primary Key - Auto-generated ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * User who owns this check-in entry
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Date of the check-in (YYYY-MM-DD)
     * Used to uniquely identify a day for a user
     */
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    /**
     * Weight for the day (in kg)
     * Optional - user may not weigh themselves daily
     */
    @Column(name = "weight")
    private Double weight;
    
    /**
     * Steps taken during the day
     * Optional
     */
    @Column(name = "steps")
    private Integer steps;
    
    /**
     * Whether a workout was completed today
     */
    @Column(name = "workout_done", nullable = false)
    private Boolean workoutDone = false;
    
    /**
     * Optional notes for the day
     */
    @Column(name = "notes", length = 1000)
    private String notes;
    
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
    public DailyCheckIn() {
    }
    
    /**
     * Constructor for creating a new daily check-in entry
     */
    public DailyCheckIn(User user, LocalDate date) {
        this.user = user;
        this.date = date;
        this.workoutDone = false;
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
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public Double getWeight() {
        return weight;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    public Integer getSteps() {
        return steps;
    }
    
    public void setSteps(Integer steps) {
        this.steps = steps;
    }
    
    public Boolean getWorkoutDone() {
        return workoutDone;
    }
    
    public void setWorkoutDone(Boolean workoutDone) {
        this.workoutDone = workoutDone;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
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
        return "DailyCheckIn{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", date=" + date +
                ", weight=" + weight +
                ", steps=" + steps +
                ", workoutDone=" + workoutDone +
                ", notes=" + (notes != null ? notes.substring(0, Math.min(50, notes.length())) : null) +
                ", createdAt=" + createdAt +
                '}';
    }
}



