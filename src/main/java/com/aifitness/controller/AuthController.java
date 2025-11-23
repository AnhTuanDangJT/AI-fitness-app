package com.aifitness.controller;

import com.aifitness.dto.ApiResponse;
import com.aifitness.dto.LoginRequest;
import com.aifitness.dto.LoginResponse;
import com.aifitness.dto.SignupRequest;
import com.aifitness.dto.UserResponse;
import com.aifitness.entity.User;
import com.aifitness.service.AccountService;
import com.aifitness.util.JwtTokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Authentication Controller
 * 
 * Handles user registration and login endpoints.
 * 
 * LOGIC TO MOVE FROM mainOne.java:
 * - Menu option 1 (Create an account) -> POST /auth/signup
 *   This includes all the profile creation logic from lines 105-330 in mainOne.java
 *   - User input collection (username, password, name, weight, height, etc.)
 *   - Profile creation with all body measurements
 *   - Activity level selection (actFloor)
 *   - Goal selection (caloGoal)
 * 
 * - Menu option 2 (Sign in) -> POST /auth/login
 *   This includes the sign-in logic from lines 332-548 in mainOne.java
 *   - Username/password verification
 *   - Password decryption logic (but will use BCrypt verification instead)
 * 
 * - Password encryption from mainOne.java:
 *   - EncryptPass() method (lines 14-32) - WILL BE REPLACED with BCrypt hashing
 *   - DecryptPass() method (lines 34-52) - WILL BE REMOVED (using BCrypt verification)
 *   - OrginalTableSign() method (lines 66-80) - WILL BE REMOVED (no longer needed)
 * 
 * NOTE: The menu loop logic (lines 95-556) will be replaced by REST endpoints.
 * The PressContinue() method (lines 7-12) is console-specific and won't be needed.
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    
    private final AccountService accountService;
    private final JwtTokenService jwtTokenService;
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration; // in milliseconds
    
    @Autowired
    public AuthController(AccountService accountService, JwtTokenService jwtTokenService) {
        this.accountService = accountService;
        this.jwtTokenService = jwtTokenService;
    }
    
    /**
     * POST /api/auth/signup
     * 
     * Creates a new user account.
     * 
     * This replaces the account creation flow from mainOne.java (lines 105-330):
     * - Validates input (username, email, password)
     * - Checks if user already exists
     * - Hashes password using BCrypt (replaces EncryptPass from mainOne.java)
     * - Creates User entity (replacing Infoclient creation)
     * - Saves to database
     * 
     * Request Body:
     * {
     *   "username": "john_doe",
     *   "email": "john@example.com",
     *   "password": "password123"
     * }
     * 
     * Success Response (201 Created):
     * {
     *   "success": true,
     *   "message": "User registered successfully",
     *   "data": {
     *     "id": 1,
     *     "username": "john_doe",
     *     "email": "john@example.com",
     *     "createdAt": "2024-01-15T10:30:00"
     *   },
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     * 
     * Error Response (400 Bad Request - Validation Error):
     * {
     *   "success": false,
     *   "message": "Validation failed",
     *   "data": {
     *     "username": "Username must be between 3 and 50 characters",
     *     "email": "Email must be valid"
     *   },
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     * 
     * Error Response (409 Conflict - User Already Exists):
     * {
     *   "success": false,
     *   "message": "Username already exists: john_doe",
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@Valid @RequestBody SignupRequest request) {
        // Register user (validates, hashes password, checks for duplicates, saves to database)
        User user = accountService.registerUser(
            request.getUsername(),
            request.getEmail(),
            request.getPassword()
        );
        
        // Convert User entity to UserResponse DTO (excludes password)
        UserResponse userResponse = new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getCreatedAt()
        );
        
        // Return success response
        ApiResponse<UserResponse> response = ApiResponse.success(
            "User registered successfully",
            userResponse
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * POST /api/auth/register (Alias for /signup)
     * 
     * Creates a new user account - same as /signup endpoint.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody SignupRequest request) {
        return signup(request);
    }
    
    /**
     * POST /api/auth/login
     * 
     * Authenticates user and returns JWT token.
     * 
     * This replaces the sign-in logic from mainOne.java (lines 332-548):
     * - Verify username/email exists
     * - Verify password hash using BCrypt (replaces DecryptPass)
     * - Generate JWT token
     * - Return token and user info
     * 
     * Request Body:
     * {
     *   "usernameOrEmail": "john_doe" or "john@example.com",
     *   "password": "password123"
     * }
     * 
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Login successful",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *     "tokenType": "Bearer",
     *     "userId": 1,
     *     "username": "john_doe",
     *     "email": "john@example.com",
     *     "expiresAt": "2024-01-16T10:30:00"
     *   },
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     * 
     * Error Response (401 Unauthorized - Invalid Credentials):
     * {
     *   "success": false,
     *   "message": "Invalid username/email or password",
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        // Authenticate user (checks username/email and verifies password hash)
        // Throws InvalidCredentialsException if credentials are invalid
        User user = accountService.authenticateUser(
            request.getUsernameOrEmail(),
            request.getPassword()
        );
        
        // Generate JWT token
        String token = jwtTokenService.generateToken(
            user.getId(),
            user.getUsername(),
            user.getEmail()
        );
        
        // Calculate expiration time
        LocalDateTime expiresAt = LocalDateTime.ofInstant(
            new Date(System.currentTimeMillis() + jwtExpiration).toInstant(),
            ZoneId.systemDefault()
        );
        
        // Create login response with token and user info
        LoginResponse loginResponse = new LoginResponse(
            token,
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            expiresAt
        );
        
        // Return success response
        ApiResponse<LoginResponse> response = ApiResponse.success(
            "Login successful",
            loginResponse
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/auth/logout
     * 
     * Logs out the current user (mainly for JWT token invalidation).
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // TODO: Implement logout logic
        // For JWT, this might just be client-side token removal
        return ResponseEntity.ok("Logout endpoint - to be implemented");
    }
}

