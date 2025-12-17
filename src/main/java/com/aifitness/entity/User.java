package com.aifitness.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
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
@Table(name = "users")
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
    @Column(name = "username", nullable = false, length = 50)
    private String username;
    
    /**
     * Email address - Must be unique
     * New field for user identification
     */
    @Column(name = "email", nullable = false, length = 100)
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
    
    /**
     * Food Preferences and Constraints
     */
    @Column(name = "dietary_preference", length = 50)
    private String dietaryPreference; // e.g., "omnivore", "vegan", "vegetarian", "halal", "kosher", etc.
    
    @Column(name = "disliked_foods", length = 500)
    private String dislikedFoods; // Comma-separated list of disliked foods
    
    @Column(name = "max_budget_per_day")
    private Integer maxBudgetPerDay; // Maximum budget per day in user's currency
    
    @Column(name = "max_cooking_time_per_meal")
    private Integer maxCookingTimePerMeal; // Maximum cooking time per meal in minutes
    
    @Column(name = "preferred_foods", length = 500)
    private String preferredFoods; // Comma-separated list of preferred foods
    
    @Column(name = "allergies", length = 500)
    private String allergies; // Comma-separated list of allergies
    
    @Column(name = "favorite_cuisines", length = 500)
    private String favoriteCuisines; // Comma-separated list of favorite cuisines
    
    /**
     * Email Verification Fields
     */
    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false;
    
    @Column(name = "email_verification_code", length = 255)
    private String emailVerificationCode;
    
    @Column(name = "email_verification_expires_at")
    private LocalDateTime emailVerificationExpiresAt;
    
    @Column(name = "verification_attempts", nullable = false)
    private Integer verificationAttempts = 0;
    
    /**
     * Gamification Fields
     */
    @Column(name = "xp", nullable = false)
    private Integer xp = 0;
    
    @Column(name = "current_streak_days", nullable = false)
    private Integer currentStreakDays = 0;
    
    @Column(name = "longest_streak_days", nullable = false)
    private Integer longestStreakDays = 0;
    
    @Column(name = "badges", length = 1000)
    private String badges = "[]";
    
    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;
    
    /**
     * Preferred Language
     * "EN" for English, "VI" for Vietnamese
     * Defaults to "EN"
     */
    @Column(name = "preferred_language", length = 2, nullable = false)
    private String preferredLanguage = "EN";
    
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
    
    // Food Preferences Getters and Setters
    
    public String getDietaryPreference() {
        return dietaryPreference;
    }
    
    public void setDietaryPreference(String dietaryPreference) {
        this.dietaryPreference = dietaryPreference;
    }
    
    public String getDislikedFoods() {
        return dislikedFoods;
    }
    
    public void setDislikedFoods(String dislikedFoods) {
        this.dislikedFoods = dislikedFoods;
    }
    
    public Integer getMaxBudgetPerDay() {
        return maxBudgetPerDay;
    }
    
    public void setMaxBudgetPerDay(Integer maxBudgetPerDay) {
        this.maxBudgetPerDay = maxBudgetPerDay;
    }
    
    public Integer getMaxCookingTimePerMeal() {
        return maxCookingTimePerMeal;
    }
    
    public void setMaxCookingTimePerMeal(Integer maxCookingTimePerMeal) {
        this.maxCookingTimePerMeal = maxCookingTimePerMeal;
    }
    
    public String getPreferredFoods() {
        return preferredFoods;
    }
    
    public void setPreferredFoods(String preferredFoods) {
        this.preferredFoods = preferredFoods;
    }
    
    public String getAllergies() {
        return allergies;
    }
    
    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }
    
    public String getFavoriteCuisines() {
        return favoriteCuisines;
    }
    
    public void setFavoriteCuisines(String favoriteCuisines) {
        this.favoriteCuisines = favoriteCuisines;
    }
    
    // Email Verification Getters and Setters
    
    public Boolean getIsEmailVerified() {
        return isEmailVerified != null ? isEmailVerified : false;
    }
    
    public void setIsEmailVerified(Boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified != null ? isEmailVerified : false;
    }
    
    public String getEmailVerificationCode() {
        return emailVerificationCode;
    }
    
    public void setEmailVerificationCode(String emailVerificationCode) {
        this.emailVerificationCode = emailVerificationCode;
    }
    
    public LocalDateTime getEmailVerificationExpiresAt() {
        return emailVerificationExpiresAt;
    }
    
    public void setEmailVerificationExpiresAt(LocalDateTime emailVerificationExpiresAt) {
        this.emailVerificationExpiresAt = emailVerificationExpiresAt;
    }
    
    public Integer getVerificationAttempts() {
        return verificationAttempts != null ? verificationAttempts : 0;
    }
    
    public void setVerificationAttempts(Integer verificationAttempts) {
        this.verificationAttempts = verificationAttempts != null ? verificationAttempts : 0;
    }
    
    // Gamification Getters and Setters
    
    public Integer getXp() {
        return xp != null ? xp : 0;
    }
    
    public void setXp(Integer xp) {
        this.xp = xp != null ? xp : 0;
    }
    
    public Integer getCurrentStreakDays() {
        return currentStreakDays != null ? currentStreakDays : 0;
    }
    
    public void setCurrentStreakDays(Integer currentStreakDays) {
        this.currentStreakDays = currentStreakDays != null ? currentStreakDays : 0;
    }
    
    public Integer getLongestStreakDays() {
        return longestStreakDays != null ? longestStreakDays : 0;
    }
    
    public void setLongestStreakDays(Integer longestStreakDays) {
        this.longestStreakDays = longestStreakDays != null ? longestStreakDays : 0;
    }
    
    public String getBadges() {
        return badges != null ? badges : "[]";
    }
    
    public void setBadges(String badges) {
        this.badges = badges != null ? badges : "[]";
    }
    
    public LocalDate getLastActivityDate() {
        return lastActivityDate;
    }
    
    public void setLastActivityDate(LocalDate lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }
    
    public String getPreferredLanguage() {
        return preferredLanguage != null ? preferredLanguage : "EN";
    }
    
    public void setPreferredLanguage(String preferredLanguage) {
        // Validate and normalize
        if (preferredLanguage != null && (preferredLanguage.equals("EN") || preferredLanguage.equals("VI"))) {
            this.preferredLanguage = preferredLanguage;
        } else {
            this.preferredLanguage = "EN"; // Default to English
        }
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

