package com.aifitness.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * User Entity - Database Model
 * 
 * This will replace Infoclient.java as the database entity.
 * 
 * LOGIC TO MOVE FROM Infoclient.java:
 * - All private fields from Infoclient (name, username, password, weight, height, etc.)
 * - All calculated fields (BMI, WHR, WHtR, BMR, TDEE, BodyFat, etc.)
 * - All getters and setters
 * - Relationships and annotations for JPA persistence
 * 
 * NOTE: Password encryption logic from mainOne.java (EncryptPass/DecryptPass) will be
 * replaced with BCrypt hashing in the service layer. The tablesign() method from
 * Infoclient.java will be removed.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username", unique = true),
    @Index(name = "idx_email", columnList = "email", unique = true)
})
public class User {
    
    /**
     * Primary Key - Auto-generated ID
     * Note: Using IDENTITY strategy for SQLite compatibility
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * Username - Must be unique
     * This replaces the UserName field from Infoclient.java
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    
    /**
     * Email address - Must be unique
     * New field for user identification
     */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
    
    /**
     * Password Hash - BCrypt hashed password
     * This replaces the password field from Infoclient.java
     * Will store BCrypt hash instead of encrypted password
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    /**
     * Created Timestamp - Automatically set on creation
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Updated Timestamp - Automatically set on update
     * Optional: For tracking last update time
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Profile Fields - From Infoclient.java
     * All fields are nullable to allow users to create accounts without profiles initially
     */
    
    @Column(name = "name", length = 100)
    private String name;
    
    @Column(name = "age")
    private Integer age;
    
    @Column(name = "sex")
    private Boolean sex; // true = male, false = female
    
    @Column(name = "weight")
    private Double weight; // in kg
    
    @Column(name = "height")
    private Double height; // in cm
    
    @Column(name = "waist")
    private Double waist; // in cm
    
    @Column(name = "hip")
    private Double hip; // in cm
    
    @Column(name = "activity_level")
    private Integer activityLevel; // 1-5
    
    @Column(name = "calorie_goal")
    private Integer calorieGoal; // 1=Lose weight, 2=Maintain, 3=Gain muscle, 4=Recomposition
    
    // Note: Calculated metrics (BMI, WHR, WHtR, BMR, TDEE, BodyFat, etc.)
    // will be calculated on-the-fly in services rather than stored in database
    // This ensures they're always up-to-date when profile data changes
    
    /**
     * Default constructor - Required by JPA
     */
    public User() {
    }
    
    /**
     * Constructor for creating a new user
     */
    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
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
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * Get password hash
     * Note: We use passwordHash instead of password to clearly indicate it's hashed
     */
    public String getPasswordHash() {
        return passwordHash;
    }
    
    /**
     * Set password hash
     * Note: This should only be called with a BCrypt hashed password
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    /**
     * Convenience method to get password hash (for compatibility)
     * This can be used if we need to access it as "password" in some contexts
     */
    public String getPassword() {
        return passwordHash;
    }
    
    /**
     * Convenience method to set password hash (for compatibility)
     * This can be used if we need to set it as "password" in some contexts
     */
    public void setPassword(String passwordHash) {
        this.passwordHash = passwordHash;
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
    
    // Additional Getters and Setters for Profile Fields
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public Boolean getSex() {
        return sex;
    }
    
    public void setSex(Boolean sex) {
        this.sex = sex;
    }
    
    public Double getWeight() {
        return weight;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    public Double getHeight() {
        return height;
    }
    
    public void setHeight(Double height) {
        this.height = height;
    }
    
    public Double getWaist() {
        return waist;
    }
    
    public void setWaist(Double waist) {
        this.waist = waist;
    }
    
    public Double getHip() {
        return hip;
    }
    
    public void setHip(Double hip) {
        this.hip = hip;
    }
    
    public Integer getActivityLevel() {
        return activityLevel;
    }
    
    public void setActivityLevel(Integer activityLevel) {
        this.activityLevel = activityLevel;
    }
    
    public Integer getCalorieGoal() {
        return calorieGoal;
    }
    
    public void setCalorieGoal(Integer calorieGoal) {
        this.calorieGoal = calorieGoal;
    }
    
    /**
     * Checks if user has a complete profile.
     */
    public boolean hasCompleteProfile() {
        return name != null && age != null && sex != null &&
               weight != null && height != null && waist != null && hip != null &&
               activityLevel != null && calorieGoal != null;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

