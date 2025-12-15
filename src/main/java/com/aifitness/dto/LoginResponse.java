package com.aifitness.dto;

import java.time.LocalDateTime;

/**
 * Login Response DTO
 * 
 * Response object for successful login containing JWT token and user information.
 */
public class LoginResponse {
    
    private String token;
    private String tokenType = "Bearer"; // Token type for Authorization header
    private Long userId;
    private String username;
    private String email;
    private LocalDateTime expiresAt;
    private Boolean isEmailVerified; // Email verification status
    
    public LoginResponse() {
    }
    
    public LoginResponse(String token, Long userId, String username, String email, LocalDateTime expiresAt) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.expiresAt = expiresAt;
    }
    
    public LoginResponse(String token, Long userId, String username, String email, LocalDateTime expiresAt, Boolean isEmailVerified) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.expiresAt = expiresAt;
        this.isEmailVerified = isEmailVerified;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
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
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Boolean getIsEmailVerified() {
        return isEmailVerified;
    }
    
    public void setIsEmailVerified(Boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
    }
}

