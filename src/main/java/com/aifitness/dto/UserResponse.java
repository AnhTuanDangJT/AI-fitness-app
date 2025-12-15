package com.aifitness.dto;

import java.time.LocalDateTime;

/**
 * User Response DTO
 * 
 * Response object for user data (excludes sensitive information like password).
 */
public class UserResponse {
    
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private Boolean isEmailVerified; // Email verification status
    
    public UserResponse() {
    }
    
    public UserResponse(Long id, String username, String email, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
    }
    
    public UserResponse(Long id, String username, String email, LocalDateTime createdAt, Boolean isEmailVerified) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.isEmailVerified = isEmailVerified;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Boolean getIsEmailVerified() {
        return isEmailVerified;
    }
    
    public void setIsEmailVerified(Boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
    }
}

