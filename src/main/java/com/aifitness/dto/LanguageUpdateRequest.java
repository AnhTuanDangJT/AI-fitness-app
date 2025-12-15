package com.aifitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Language Update Request DTO
 * 
 * Request body for updating user's preferred language.
 */
public class LanguageUpdateRequest {
    
    @NotBlank(message = "Language is required")
    @Pattern(regexp = "^(EN|VI)$", message = "Language must be either 'EN' or 'VI'")
    private String language;
    
    // Getters and setters
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
}


