package com.aifitness.controller;

import com.aifitness.dto.ApiResponse;
import com.aifitness.dto.FullAnalysisResponse;
import com.aifitness.dto.ProfileResponseDTO;
import com.aifitness.dto.ProfileSaveRequest;
import com.aifitness.dto.ProfileUpdateRequest;
import com.aifitness.entity.User;
import com.aifitness.repository.UserRepository;
import com.aifitness.service.BodyMetricsService;
import com.aifitness.service.NutritionService;
import com.aifitness.service.ProfileService;
import com.aifitness.util.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Profile Controller
 * 
 * Handles profile creation and updates.
 * 
 * LOGIC TO MOVE FROM mainOne.java:
 * - Profile creation flow (lines 111-330)
 * - Creates Infoclient object with all profile data and calculations
 */
@RestController
@RequestMapping("/profile")
@CrossOrigin(origins = "http://localhost:3000")
public class ProfileController {
    
    private final ProfileService profileService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final BodyMetricsService bodyMetricsService;
    private final NutritionService nutritionService;
    
    @Autowired
    public ProfileController(ProfileService profileService,
                             JwtTokenService jwtTokenService,
                             UserRepository userRepository,
                             BodyMetricsService bodyMetricsService,
                             NutritionService nutritionService) {
        this.profileService = profileService;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.bodyMetricsService = bodyMetricsService;
        this.nutritionService = nutritionService;
    }
    
    /**
     * Helper method to extract and validate user from JWT token.
     */
    private User getAuthenticatedUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized: No token provided");
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        if (!jwtTokenService.validateToken(token)) {
            throw new RuntimeException("Unauthorized: Invalid token");
        }
        
        Long userId = jwtTokenService.getUserIdFromToken(token);
        
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * Helper method to check if user has required profile data.
     */
    private void validateProfileData(User user) {
        if (!user.hasCompleteProfile()) {
            throw new RuntimeException("Profile incomplete. Please complete your profile setup first.");
        }
    }
    
    /**
     * POST /api/profile/save
     * 
     * Saves or updates user profile.
     * 
     * This replaces the profile creation flow from mainOne.java (lines 111-330):
     * - Collects all profile information (name, weight, height, age, etc.)
     * - Saves to database under userId
     * - If profile exists, updates it
     * 
     * Request Body:
     * {
     *   "name": "John Doe",
     *   "weight": 75.5,
     *   "height": 175.0,
     *   "age": 30,
     *   "waist": 80.0,
     *   "hip": 95.0,
     *   "sex": true,  // true = male, false = female
     *   "activityLevel": 3,
     *   "goal": 2
     * }
     * 
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Profile saved successfully",
     *   "data": {
     *     "id": 1,
     *     "name": "John Doe",
     *     ...
     *   },
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     * 
     * Error Response (400 Bad Request - Validation Error):
     * {
     *   "success": false,
     *   "message": "Validation failed",
     *   "data": {
     *     "weight": "Weight must be positive",
     *     "age": "Age must be at least 1"
     *   },
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<ProfileResponseDTO>> saveProfile(
            @Valid @RequestBody ProfileSaveRequest request,
            HttpServletRequest httpRequest) {
        
        // Extract JWT token from Authorization header
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error("Unauthorized: No token provided")
            );
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        // Validate token and extract user ID
        if (!jwtTokenService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error("Unauthorized: Invalid token")
            );
        }
        
        Long userId = jwtTokenService.getUserIdFromToken(token);
        
        // Save or update profile (handles both new profile creation and updates)
        User user = profileService.saveProfile(userId, request);
        
        // Get updated profile with all calculations
        ProfileResponseDTO profile = profileService.getProfile(userId);
        
        // Return success response
        ApiResponse<ProfileResponseDTO> response = ApiResponse.success(
            "Profile saved successfully",
            profile
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/profile/full-analysis
     * 
     * Returns a comprehensive analysis combining all health calculations.
     * 
     * This endpoint merges all calculation endpoints into a single response:
     * - BMI and BMI category
     * - WHR and WHR risk level
     * - BMR (Basal Metabolic Rate)
     * - TDEE (Total Daily Energy Expenditure)
     * - Goal calories
     * - Protein target
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Full analysis calculated successfully",
     *   "data": {
     *     "bmi": 23.4,
     *     "bmiCategory": "Normal",
     *     "whr": 0.85,
     *     "whrRisk": "Good condition",
     *     "bmr": 1650.0,
     *     "tdee": 2200.0,
     *     "goalCalories": 2000.0,
     *     "proteinTarget": 140.0
     *   },
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     */
    @GetMapping("/full-analysis")
    public ResponseEntity<ApiResponse<FullAnalysisResponse>> getFullAnalysis(HttpServletRequest request) {
        try {
            // Get authenticated user
            User user = getAuthenticatedUser(request);
            validateProfileData(user);
            
            // Extract profile data
            double weight = user.getWeight();
            double height = user.getHeight();
            double waist = user.getWaist();
            double hip = user.getHip();
            int age = user.getAge();
            boolean isMale = user.getSex() != null && user.getSex();
            int activityLevel = user.getActivityLevel();
            int calorieGoal = user.getCalorieGoal();
            
            // Calculate BMI
            double bmi = bodyMetricsService.calculateBMI(weight, height);
            String bmiCategory = bodyMetricsService.getBMICategory(bmi);
            
            // Calculate WHR
            double whr = bodyMetricsService.calculateWHR(waist, hip);
            String whrRisk = bodyMetricsService.assessWHRHealth(whr, isMale);
            
            // Calculate BMR
            double bmr = nutritionService.calculateBMR(weight, height, age, isMale);
            
            // Calculate TDEE
            double tdee = nutritionService.calculateTDEE(bmr, activityLevel);
            
            // Calculate goal calories
            double goalCalories = nutritionService.calculateGoalCalories(tdee, calorieGoal);
            
            // Calculate protein target
            double proteinTarget = nutritionService.calculateProtein(calorieGoal, weight);
            
            // Create response
            FullAnalysisResponse response = new FullAnalysisResponse(
                bmi,
                bmiCategory,
                whr,
                whrRisk,
                bmr,
                tdee,
                goalCalories,
                proteinTarget
            );
            
            return ResponseEntity.ok(ApiResponse.success("Full analysis calculated successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while calculating full analysis: " + e.getMessage()));
        }
    }
    
    /**
     * PUT /api/profile/update
     * 
     * Updates specific profile fields (partial update).
     * 
     * This allows users to update individual fields like weight, height, goal, or activity level.
     * Only provided fields will be updated; other fields remain unchanged.
     * 
     * Request Body (all fields optional):
     * {
     *   "weight": 76.0,
     *   "height": 176.0,
     *   "activityLevel": 4,
     *   "goal": 3
     * }
     * 
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Profile updated successfully",
     *   "data": {
     *     ... (full profile with recalculated values)
     *   },
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<ProfileResponseDTO>> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            // Extract JWT token from Authorization header
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Unauthorized: No token provided")
                );
            }
            
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            
            // Validate token and extract user ID
            if (!jwtTokenService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Unauthorized: Invalid token")
                );
            }
            
            Long userId = jwtTokenService.getUserIdFromToken(token);
            
            // Validate that at least one field is provided
            if (!request.hasUpdates()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.error("No fields provided for update")
                );
            }
            
            // Update profile (partial update)
            User user = profileService.updateProfile(userId, request);
            
            // Get updated profile with all recalculated values
            ProfileResponseDTO profile = profileService.getProfile(userId);
            
            // Return success response
            ApiResponse<ProfileResponseDTO> response = ApiResponse.success(
                "Profile updated successfully",
                profile
            );
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while updating profile: " + e.getMessage()));
        }
    }
}

