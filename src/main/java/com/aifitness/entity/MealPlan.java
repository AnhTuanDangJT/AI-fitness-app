package com.aifitness.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Meal Plan Entity
 * 
 * Represents a weekly meal plan for a user.
 * Contains multiple meal plan entries (one per meal per day).
 */
@Entity
@Table(name = "meal_plans", indexes = {
    @Index(name = "idx_user_week", columnList = "user_id, week_start_date"),
    @Index(name = "idx_user_created", columnList = "user_id, created_at")
})
public class MealPlan {
    
    /**
     * Primary Key - Auto-generated ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * User who owns this meal plan
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
     * Meal plan entries (one-to-many relationship)
     * Cascade: When meal plan is deleted, entries are also deleted
     */
    @OneToMany(mappedBy = "mealPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MealPlanEntry> entries = new ArrayList<>();
    
    /**
     * Created Timestamp - Automatically set on creation
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Default constructor - Required by JPA
     */
    public MealPlan() {
    }
    
    /**
     * Constructor for creating a new meal plan
     */
    public MealPlan(User user, LocalDate weekStartDate) {
        this.user = user;
        this.weekStartDate = weekStartDate;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * JPA Lifecycle Callback - Set createdAt before persisting
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
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
    
    public List<MealPlanEntry> getEntries() {
        return entries;
    }
    
    public void setEntries(List<MealPlanEntry> entries) {
        this.entries = entries;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Helper method to add an entry to the meal plan
     */
    public void addEntry(MealPlanEntry entry) {
        entries.add(entry);
        entry.setMealPlan(this);
    }
    
    @Override
    public String toString() {
        return "MealPlan{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", weekStartDate=" + weekStartDate +
                ", entriesCount=" + (entries != null ? entries.size() : 0) +
                ", createdAt=" + createdAt +
                '}';
    }
}






