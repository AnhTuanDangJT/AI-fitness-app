package com.aifitness.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * AI Configuration
 * 
 * Manages configuration for AI service integrations.
 * 
 * This class reads AI API keys and configuration from environment variables.
 * All AI API calls will happen only in the backend - the frontend will never
 * directly access AI services.
 * 
 * Environment Variables:
 * - AI_API_KEY: API key for AI service provider (e.g., OpenAI, Anthropic)
 * 
 * Usage:
 * - Inject this configuration into AI service classes
 * - Use the API key to initialize AI client libraries
 * - Never expose the API key to the frontend
 * 
 * Example:
 * ```java
 * @Autowired
 * private AiConfig aiConfig;
 * 
 * String apiKey = aiConfig.getApiKey();
 * // Initialize AI client with apiKey
 * ```
 */
@Configuration
public class AiConfig {
    
    /**
     * AI API Key from environment variable.
     *
     * Set this in your environment:
     * - Development: Set in application.properties or environment variable
     * - Production: Set as environment variable (e.g., AI_API_KEY=sk-...)
     *
     * DO NOT hardcode the API key in this file or commit it to version control.
     */
    @Value("${AI_API_KEY:}")
    private String apiKey;

    /**
     * GitHub token used for shared AI helper (GitHub Models / Copilot).
     */
    @Value("${GITHUB_TOKEN:}")
    private String githubToken;
    
    /**
     * AI API Base URL (optional, for custom endpoints).
     * Defaults to provider's standard endpoint if not set.
     */
    @Value("${AI_API_BASE_URL:}")
    private String apiBaseUrl;
    
    /**
     * AI Model to use (e.g., "gpt-4", "gpt-3.5-turbo", "claude-3-opus").
     * Can be overridden per service if needed.
     */
    @Value("${AI_MODEL:gpt-4}")
    private String model;

    /**
     * GitHub model identifier (defaults to gpt-4o-mini).
     */
    @Value("${GITHUB_MODEL:gpt-4o-mini}")
    private String githubModel;
    
    /**
     * Maximum tokens for AI responses (default: 2000).
     */
    @Value("${AI_MAX_TOKENS:2000}")
    private int maxTokens;
    
    /**
     * Temperature for AI responses (0.0-2.0, default: 0.7).
     * Lower = more deterministic, Higher = more creative.
     */
    @Value("${AI_TEMPERATURE:0.7}")
    private double temperature;
    
    /**
     * Gets the AI API key.
     * 
     * @return API key, or empty string if not set
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Gets the GitHub token used for shared AI calls.
     */
    public String getGithubToken() {
        return githubToken;
    }
    
    /**
     * Checks if AI API key is configured.
     * 
     * @return true if API key is set, false otherwise
     */
    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
    
    /**
     * Gets the AI API base URL.
     * 
     * @return Base URL, or empty string if using default
     */
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }
    
    /**
     * Gets the AI model name.
     * 
     * @return Model name
     */
    public String getModel() {
        return model;
    }

    /**
     * Gets the GitHub model name.
     */
    public String getGithubModel() {
        return githubModel;
    }
    
    /**
     * Gets the maximum tokens for AI responses.
     * 
     * @return Maximum tokens
     */
    public int getMaxTokens() {
        return maxTokens;
    }
    
    /**
     * Gets the temperature for AI responses.
     * 
     * @return Temperature (0.0-2.0)
     */
    public double getTemperature() {
        return temperature;
    }
    
    /**
     * Validates that AI configuration is properly set up.
     * 
     * @throws IllegalStateException if API key is not configured
     */
    public void validateConfiguration() {
        if (!isApiKeyConfigured()) {
            throw new IllegalStateException(
                "AI_API_KEY environment variable is not set. " +
                "Please set it before using AI services."
            );
        }
    }
}

