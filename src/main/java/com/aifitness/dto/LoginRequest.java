package com.aifitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Login Request DTO
 * 
 * Request body for user login endpoint.
 * 
 * Supports login with either username or email.
 * Includes validation to prevent injection attacks.
 */
public class LoginRequest {
    
    @NotBlank(message = "Username or email is required")
    @Size(max = 100, message = "Username or email must not exceed 100 characters")
    // Allow alphanumeric, underscore, @, dot, and common email chars
    @Pattern(regexp = "^[a-zA-Z0-9_@.+-]+$", message = "Username or email contains invalid characters")
    private String usernameOrEmail; // Can be username or email
    
    @NotBlank(message = "Password is required")
    @Size(max = 100, message = "Password must not exceed 100 characters")
    // Password validation: allow alphanumeric and common special chars, but reject dangerous patterns
    @Pattern(regexp = "^[^<>\"'%;()&|`$\\\\]*$", message = "Password contains invalid characters")
    private String password;
    
    // Constructors
    public LoginRequest() {
    }
    
    public LoginRequest(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }
    
    // Getters and Setters
    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }
    
    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Checks if the usernameOrEmail field looks like an email.
     */
    public boolean isEmail() {
        return usernameOrEmail != null && usernameOrEmail.contains("@");
    }
}

