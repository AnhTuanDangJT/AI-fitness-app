package com.aifitness.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login Request DTO
 * 
 * Request body for user login endpoint.
 * 
 * Supports login with either username or email.
 */
public class LoginRequest {
    
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail; // Can be username or email
    
    @NotBlank(message = "Password is required")
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

