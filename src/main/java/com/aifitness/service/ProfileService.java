package com.aifitness.service;

import com.aifitness.dto.*;
import com.aifitness.entity.User;
import com.aifitness.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Profile Service
 * 
 * Handles user profile CRUD operations and coordinates metric calculations.
 * 
 * LOGIC TO MOVE FROM mainOne.java:
 * - Profile information display (line 407: clientSignin.toString())
 * - Profile updates (all update operations from lines 388-544)
 * 
 * This service coordinates between AccountService, BodyMetricsService, and NutritionService
 * to ensure all dependent metrics are recalculated when profile fields change.
 */
@Service
@Transactional
public class ProfileService {
    
    private final UserRepository userRepository;
    private final BodyMetricsService bodyMetricsService;
    private final NutritionService nutritionService;
    
    @Autowired
    public ProfileService(UserRepository userRepository, 
                         BodyMetricsService bodyMetricsService,
                         NutritionService nutritionService) {
        this.userRepository = userRepository;
        this.bodyMetricsService = bodyMetricsService;
        this.nutritionService = nutritionService;
    }
    
    /**
     * Retrieves complete user profile with all calculations.
     * 
     * LOGIC TO MOVE FROM Infoclient.java:
     * - toString() method (lines 443-483)
     *   Formats complete profile display
     * 
     * Uses actual user profile data if available, otherwise returns mock data for testing.
     */
    public ProfileResponseDTO getProfile(Long userId) {
        // Get user from database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Use actual profile data if available, otherwise use mock data for testing
        if (user.hasCompleteProfile()) {
            return createProfileFromUser(user);
        } else {
            // Return mock profile for testing (until user completes profile setup)
            return createMockProfile(user);
        }
    }
    
    /**
     * Saves or updates user profile.
     * 
     * LOGIC TO MOVE FROM mainOne.java:
     * - Profile creation flow (lines 111-330)
     * - Profile update logic from sign-in menu
     * 
     * @param userId The user ID
     * @param request Profile data to save
     * @return Updated user entity
     */
    public User saveProfile(Long userId, ProfileSaveRequest request) {
        // Find user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update profile fields (this will update if profile exists, or create new fields)
        user.setName(request.getName());
        user.setAge(request.getAge());
        user.setSex(request.getSex());
        user.setWeight(request.getWeight());
        user.setHeight(request.getHeight());
        user.setWaist(request.getWaist());
        user.setHip(request.getHip());
        user.setActivityLevel(request.getActivityLevel());
        user.setCalorieGoal(request.getGoal());
        
        // Save to database (JPA will update existing user)
        user = userRepository.save(user);
        
        // All calculations will be done when profile is retrieved via getProfile()
        // This keeps calculations always up-to-date
        
        return user;
    }
    
    /**
     * Updates specific profile fields (partial update).
     * 
     * LOGIC TO MOVE FROM mainOne.java:
     * - Profile update operations from sign-in menu (lines 388-544)
     * - Individual field updates (weight, height, goal, activity level, etc.)
     * 
     * @param userId The user ID
     * @param request Profile update data (only provided fields will be updated)
     * @return Updated user entity
     */
    public User updateProfile(Long userId, ProfileUpdateRequest request) {
        // Find user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update only provided fields (partial update)
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getWeight() != null) {
            user.setWeight(request.getWeight());
        }
        if (request.getHeight() != null) {
            user.setHeight(request.getHeight());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (request.getWaist() != null) {
            user.setWaist(request.getWaist());
        }
        if (request.getHip() != null) {
            user.setHip(request.getHip());
        }
        if (request.getSex() != null) {
            user.setSex(request.getSex());
        }
        if (request.getActivityLevel() != null) {
            user.setActivityLevel(request.getActivityLevel());
        }
        if (request.getGoal() != null) {
            user.setCalorieGoal(request.getGoal());
        }
        
        // Save to database (JPA will update existing user)
        user = userRepository.save(user);
        
        // All calculations will be done when profile is retrieved via getProfile()
        // This keeps calculations always up-to-date
        
        return user;
    }
    
    /**
     * Creates mock profile for testing Feature 1.
     * This matches the structure from Infoclient.java toString() method.
     * 
     * Now uses actual user profile data if available, otherwise falls back to mock data.
     */
    private ProfileResponseDTO createMockProfile(User user) {
        // If user has complete profile, use actual data; otherwise use mock data
        boolean useActualData = user.hasCompleteProfile();
        
        if (useActualData) {
            return createProfileFromUser(user);
        }
        
        // Fallback to mock data for testing
        ProfileResponseDTO profile = new ProfileResponseDTO();
        
        // Basic Information
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setEmail(user.getEmail());
        profile.setName("John Doe"); // Mock data
        profile.setAge(30); // Mock data
        profile.setSex("Male"); // Mock data
        profile.setCreatedAt(user.getCreatedAt());
        profile.setUpdatedAt(user.getUpdatedAt());
        
        // Body Measurements (Mock data matching Java program example)
        double weight = 75.5;
        double height = 175.0;
        double waist = 80.0;
        double hip = 95.0;
        boolean isMale = true;
        int age = 30;
        int activityLevel = 3;
        int calorieGoal = 2;
        
        profile.setWeight(weight);
        profile.setHeight(height);
        profile.setWaist(waist);
        profile.setHip(hip);
        profile.setActivityLevel(activityLevel);
        profile.setActivityLevelName("Moderately active (3–5×/week)");
        profile.setCalorieGoal(calorieGoal);
        profile.setCalorieGoalName("Maintain weight");
        
        // Calculate Body Metrics
        BodyMetricsDTO bodyMetrics = new BodyMetricsDTO();
        double bmi = bodyMetricsService.calculateBMI(weight, height);
        bodyMetrics.setBmi(bmi);
        bodyMetrics.setBmiCategory(bodyMetricsService.getBMICategory(bmi));
        
        double whr = bodyMetricsService.calculateWHR(waist, hip);
        bodyMetrics.setWhr(whr);
        bodyMetrics.setWhrHealthStatus(bodyMetricsService.assessWHRHealth(whr, isMale));
        
        double whtr = bodyMetricsService.calculateWHtR(waist, height);
        bodyMetrics.setWhtr(whtr);
        bodyMetrics.setWhtrRiskLevel(bodyMetricsService.analyzeWHtRRisk(whtr));
        
        double bodyFat = bodyMetricsService.calculateBodyFat(bmi, age, isMale);
        bodyMetrics.setBodyFat(bodyFat);
        
        profile.setBodyMetrics(bodyMetrics);
        
        // Calculate Energy
        EnergyDTO energy = new EnergyDTO();
        double bmr = nutritionService.calculateBMR(weight, height, age, isMale);
        energy.setBmr(bmr);
        
        double tdee = nutritionService.calculateTDEE(bmr, activityLevel);
        energy.setTdee(tdee);
        
        double goalCalories = nutritionService.calculateGoalCalories(tdee, calorieGoal);
        energy.setGoalCalories(goalCalories);
        
        profile.setEnergy(energy);
        
        // Calculate Macronutrients
        MacronutrientsDTO macros = new MacronutrientsDTO();
        double protein = nutritionService.calculateProtein(calorieGoal, weight);
        macros.setProtein(protein);
        
        double fat = nutritionService.calculateFat(weight);
        macros.setFat(fat);
        
        double carbs = nutritionService.calculateCarbs(goalCalories, protein, fat);
        macros.setCarbohydrates(carbs);
        
        macros.setFiber(nutritionService.getFiber(isMale));
        macros.setWater(nutritionService.getWater(isMale));
        
        profile.setMacronutrients(macros);
        
        // Get Micronutrients
        MicronutrientsDTO micros = nutritionService.getMicronutrients(isMale);
        profile.setMicronutrients(micros);
        
        return profile;
    }
    
    /**
     * Creates profile response from actual user data.
     * 
     * LOGIC FROM Infoclient.java toString() method (lines 443-483)
     */
    private ProfileResponseDTO createProfileFromUser(User user) {
        ProfileResponseDTO profile = new ProfileResponseDTO();
        
        // Basic Information
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setEmail(user.getEmail());
        profile.setName(user.getName());
        profile.setAge(user.getAge());
        profile.setSex(user.getSex() != null && user.getSex() ? "Male" : "Female");
        profile.setCreatedAt(user.getCreatedAt());
        profile.setUpdatedAt(user.getUpdatedAt());
        
        // Body Measurements
        double weight = user.getWeight();
        double height = user.getHeight();
        double waist = user.getWaist();
        double hip = user.getHip();
        boolean isMale = user.getSex() != null && user.getSex();
        int age = user.getAge();
        int activityLevel = user.getActivityLevel();
        int calorieGoal = user.getCalorieGoal();
        
        profile.setWeight(weight);
        profile.setHeight(height);
        profile.setWaist(waist);
        profile.setHip(hip);
        profile.setActivityLevel(activityLevel);
        profile.setActivityLevelName(getActivityLevelName(activityLevel));
        profile.setCalorieGoal(calorieGoal);
        profile.setCalorieGoalName(getCalorieGoalName(calorieGoal));
        
        // Calculate Body Metrics
        BodyMetricsDTO bodyMetrics = new BodyMetricsDTO();
        double bmi = bodyMetricsService.calculateBMI(weight, height);
        bodyMetrics.setBmi(bmi);
        bodyMetrics.setBmiCategory(bodyMetricsService.getBMICategory(bmi));
        
        double whr = bodyMetricsService.calculateWHR(waist, hip);
        bodyMetrics.setWhr(whr);
        bodyMetrics.setWhrHealthStatus(bodyMetricsService.assessWHRHealth(whr, isMale));
        
        double whtr = bodyMetricsService.calculateWHtR(waist, height);
        bodyMetrics.setWhtr(whtr);
        bodyMetrics.setWhtrRiskLevel(bodyMetricsService.analyzeWHtRRisk(whtr));
        
        double bodyFat = bodyMetricsService.calculateBodyFat(bmi, age, isMale);
        bodyMetrics.setBodyFat(bodyFat);
        
        profile.setBodyMetrics(bodyMetrics);
        
        // Calculate Energy
        EnergyDTO energy = new EnergyDTO();
        double bmr = nutritionService.calculateBMR(weight, height, age, isMale);
        energy.setBmr(bmr);
        
        double tdee = nutritionService.calculateTDEE(bmr, activityLevel);
        energy.setTdee(tdee);
        
        double goalCalories = nutritionService.calculateGoalCalories(tdee, calorieGoal);
        energy.setGoalCalories(goalCalories);
        
        profile.setEnergy(energy);
        
        // Calculate Macronutrients
        MacronutrientsDTO macros = new MacronutrientsDTO();
        double protein = nutritionService.calculateProtein(calorieGoal, weight);
        macros.setProtein(protein);
        
        double fat = nutritionService.calculateFat(weight);
        macros.setFat(fat);
        
        double carbs = nutritionService.calculateCarbs(goalCalories, protein, fat);
        macros.setCarbohydrates(carbs);
        
        macros.setFiber(nutritionService.getFiber(isMale));
        macros.setWater(nutritionService.getWater(isMale));
        
        profile.setMacronutrients(macros);
        
        // Get Micronutrients
        MicronutrientsDTO micros = nutritionService.getMicronutrients(isMale);
        profile.setMicronutrients(micros);
        
        return profile;
    }
    
    /**
     * Gets activity level name from number.
     */
    private String getActivityLevelName(int activityLevel) {
        switch (activityLevel) {
            case 1:
                return "Sedentary (no exercise)";
            case 2:
                return "Lightly active (1–3×/week)";
            case 3:
                return "Moderately active (3–5×/week)";
            case 4:
                return "Very active (6–7×/week)";
            case 5:
                return "Extra active (2×/day)";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Gets calorie goal name from number.
     */
    private String getCalorieGoalName(int goal) {
        switch (goal) {
            case 1:
                return "Lose weight";
            case 2:
                return "Maintain weight";
            case 3:
                return "Gain muscle";
            case 4:
                return "Gain muscle and lose fat";
            default:
                return "Unknown";
        }
    }
    
}
