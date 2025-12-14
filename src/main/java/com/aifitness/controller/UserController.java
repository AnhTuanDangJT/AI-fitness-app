package com.aifitness.controller;

import com.aifitness.dto.ApiResponse;
import com.aifitness.dto.ProfileResponseDTO;
import com.aifitness.entity.User;
import com.aifitness.repository.UserRepository;
import com.aifitness.service.ProfileService;
import com.aifitness.util.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * User Profile Controller
 * 
 * Handles all user profile-related endpoints after authentication.
 * 
 * LOGIC TO MOVE FROM mainOne.java:
 * - Post-login menu options (lines 388-544) -> Various PUT endpoints
 *   - Option 1: See your information -> GET /user/profile
 *   - Option 2-12: Update various fields -> PUT /user/{field}
 * 
 * Each update operation from the profile edit menu will become a separate endpoint:
 * - Update Password (lines 410-449) -> PUT /user/password
 * - Update Username (lines 450-456) -> PUT /user/username
 * - Update Name (lines 458-464) -> PUT /user/name
 * - Update Age (lines 465-469) -> PUT /user/age
 * - Update Sex (lines 471-478) -> PUT /user/sex
 * - Update Weight (lines 480-486) -> PUT /user/weight
 * - Update Height (lines 487-493) -> PUT /user/height
 * - Update Waist (lines 495-500) -> PUT /user/waist
 * - Update Hip (lines 502-507) -> PUT /user/hip
 * - Update Calorie Goal (lines 509-519) -> PUT /user/goal
 * - Update Activity Level (lines 521-532) -> PUT /user/activity
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    
    private final ProfileService profileService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    
    @Autowired
    public UserController(ProfileService profileService, JwtTokenService jwtTokenService, UserRepository userRepository) {
        this.profileService = profileService;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
    }
    
    /**
     * GET /api/user/profile
     * 
     * Returns the complete user profile.
     * 
     * This replaces the "See your information" option from mainOne.java (line 407).
     * Uses Infoclient.java toString() method logic to format the response.
     * 
     * FEATURE 1 IMPLEMENTATION:
     * - Retrieves user ID from JWT token
     * - Calls ProfileService to get complete profile with all calculations
     * - Returns formatted profile matching Java program output
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponseDTO>> getProfile(HttpServletRequest request) {
        // Extract JWT token from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("Unauthorized: No token provided")
            );
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        // Validate token and extract user ID
        if (!jwtTokenService.validateToken(token)) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("Unauthorized: Invalid token")
            );
        }
        
        Long userId = jwtTokenService.getUserIdFromToken(token);
        
        // Get profile from service (includes all calculations)
        // If user has complete profile, returns actual data; otherwise returns mock data
        ProfileResponseDTO profile = profileService.getProfile(userId);
        
        // Return success response
        ApiResponse<ProfileResponseDTO> response = ApiResponse.success(
            "Profile retrieved successfully",
            profile
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/user/profile/complete
     * 
     * Checks if the user has a complete profile.
     * Returns a simple boolean indicating profile completeness.
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Profile completeness check",
     *   "data": {
     *     "isComplete": true
     *   }
     * }
     */
    @GetMapping("/profile/complete")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkProfileComplete(HttpServletRequest request) {
        // Extract JWT token from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("Unauthorized: No token provided")
            );
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        // Validate token and extract user ID
        if (!jwtTokenService.validateToken(token)) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("Unauthorized: Invalid token")
            );
        }
        
        Long userId = jwtTokenService.getUserIdFromToken(token);
        
        // Get user from repository
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check profile completeness
        boolean isComplete = user.hasCompleteProfile();
        
        Map<String, Boolean> data = new HashMap<>();
        data.put("isComplete", isComplete);
        
        ApiResponse<Map<String, Boolean>> response = ApiResponse.success(
            "Profile completeness check",
            data
        );
        
        return ResponseEntity.ok(response);
    }
    
    // TODO: Future endpoints for individual field updates
    // These endpoints will be implemented later when needed
    // For now, profile updates are handled through ProfileController
    // 
    // Placeholder endpoints removed to fix compilation errors.
    // DTO classes (UpdateWeightRequest, etc.) need to be created before these endpoints can be used.
}

