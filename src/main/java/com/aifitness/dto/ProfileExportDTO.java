package com.aifitness.dto;

import com.aifitness.util.StringSanitizer;

/**
 * Profile Export DTO
 * 
 * Sanitized profile data for export - excludes sensitive information
 * like internal IDs, password hashes, tokens, etc.
 * 
 * Only includes fields visible in the Profile Edit form:
 * - name, email, age, weight, height, waist, hip, sex
 * - activityLevel, activityLevelName, calorieGoal, calorieGoalName
 */
public class ProfileExportDTO {
    
    // Basic Information (visible in EditProfile form)
    private String email;
    private String name;
    private Integer age;
    private String sex;
    
    // Body Measurements (visible in EditProfile form)
    private Double weight;
    private Double height;
    private Double waist;
    private Double hip;
    
    // Settings (visible in EditProfile form)
    private Integer activityLevel;
    private String activityLevelName;
    private Integer calorieGoal;
    private String calorieGoalName;
    
    // Constructors
    public ProfileExportDTO() {
    }
    
    /**
     * Creates a sanitized export DTO from ProfileResponseDTO.
     * Excludes: id, username, password hash, tokens, timestamps, and calculated metrics.
     * Only includes fields visible in the Profile Edit form.
     * All strings are sanitized.
     */
    public static ProfileExportDTO fromProfileResponseDTO(ProfileResponseDTO profile) {
        ProfileExportDTO export = new ProfileExportDTO();
        
        // Sanitize all string fields
        export.setEmail(StringSanitizer.sanitize(profile.getEmail()));
        export.setName(StringSanitizer.sanitize(profile.getName()));
        export.setAge(profile.getAge());
        export.setSex(StringSanitizer.sanitize(profile.getSex()));
        
        export.setWeight(profile.getWeight());
        export.setHeight(profile.getHeight());
        export.setWaist(profile.getWaist());
        export.setHip(profile.getHip());
        
        export.setActivityLevel(profile.getActivityLevel());
        export.setActivityLevelName(StringSanitizer.sanitize(profile.getActivityLevelName()));
        export.setCalorieGoal(profile.getCalorieGoal());
        export.setCalorieGoalName(StringSanitizer.sanitize(profile.getCalorieGoalName()));
        
        return export;
    }
    
    // Getters and Setters
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
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
    
    public String getSex() {
        return sex;
    }
    
    public void setSex(String sex) {
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
    
    public String getActivityLevelName() {
        return activityLevelName;
    }
    
    public void setActivityLevelName(String activityLevelName) {
        this.activityLevelName = activityLevelName;
    }
    
    public Integer getCalorieGoal() {
        return calorieGoal;
    }
    
    public void setCalorieGoal(Integer calorieGoal) {
        this.calorieGoal = calorieGoal;
    }
    
    public String getCalorieGoalName() {
        return calorieGoalName;
    }
    
    public void setCalorieGoalName(String calorieGoalName) {
        this.calorieGoalName = calorieGoalName;
    }
}

