package com.aifitness.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Gamification Event Entity
 * 
 * Tracks gamification events to prevent double-counting rewards.
 * Each event is uniquely identified by (user, type, sourceId).
 */
@Entity
@Table(name = "gamification_events")
public class GamificationEvent {
    
    /**
     * Primary Key - Auto-generated ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * User who triggered this event
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Type of event
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private EventType type;
    
    /**
     * Date when the event occurred (activity date)
     */
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;
    
    /**
     * Source ID - identifies the specific entity that triggered the event
     * (e.g., weeklyProgressId, mealPlanId, bodyAnalysisId)
     */
    @Column(name = "source_id", nullable = false, length = 255)
    private String sourceId;
    
    /**
     * Created Timestamp - When the event was recorded
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Default constructor - Required by JPA
     */
    public GamificationEvent() {
    }
    
    /**
     * Constructor for creating a new gamification event
     */
    public GamificationEvent(User user, EventType type, LocalDate eventDate, String sourceId) {
        this.user = user;
        this.type = type;
        this.eventDate = eventDate;
        this.sourceId = sourceId;
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
    
    public EventType getType() {
        return type;
    }
    
    public void setType(EventType type) {
        this.type = type;
    }
    
    public LocalDate getEventDate() {
        return eventDate;
    }
    
    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }
    
    public String getSourceId() {
        return sourceId;
    }
    
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "GamificationEvent{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", type=" + type +
                ", eventDate=" + eventDate +
                ", sourceId='" + sourceId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}



