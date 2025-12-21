package com.aifitness.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Client
 * 
 * Handles communication with OpenAI API for meal plan generation.
 */
@Component
public class OpenAiClient {
    
    private final AiConfig aiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    
    @Autowired
    public OpenAiClient(AiConfig aiConfig) {
        this.aiConfig = aiConfig;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Calls OpenAI API to generate meal plan based on prompt.
     * 
     * @param prompt The prompt to send to OpenAI
     * @return The generated meal plan JSON string
     * @throws RuntimeException if API call fails
     */
    public String generateMealPlan(String prompt) {
        if (!aiConfig.isApiKeyConfigured()) {
            throw new RuntimeException("AI_API_KEY is not configured. Cannot generate meal plan.");
        }
        
        try {
            // Build request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiConfig.getModel());
            requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are a professional nutritionist and meal planning expert. Always respond with valid JSON only."),
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.3); // Lower temperature for more consistent adherence to constraints
            requestBody.put("max_tokens", aiConfig.getMaxTokens());
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiConfig.getApiKey());
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // Make API call
            ResponseEntity<String> response = restTemplate.exchange(
                OPENAI_API_URL,
                HttpMethod.POST,
                request,
                String.class
            );
            
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("OpenAI API returned error: " + response.getStatusCode());
            }
            
            // Parse response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode choices = responseJson.get("choices");
            if (choices == null || !choices.isArray() || choices.size() == 0) {
                throw new RuntimeException("OpenAI API returned invalid response format");
            }
            
            JsonNode message = choices.get(0).get("message");
            if (message == null) {
                throw new RuntimeException("OpenAI API returned invalid response format");
            }
            
            String content = message.get("content").asText();
            
            // Extract JSON from response (handle markdown code blocks if present)
            if (content.trim().startsWith("```")) {
                int start = content.indexOf("```");
                int end = content.lastIndexOf("```");
                if (start >= 0 && end > start) {
                    content = content.substring(start + 3, end).trim();
                    // Remove language identifier if present
                    if (content.startsWith("json")) {
                        content = content.substring(4).trim();
                    }
                }
            }
            
            return content;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to call OpenAI API: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calls OpenAI API to generate a natural language coach response.
     * 
     * @param systemPrompt Behavior + policy instructions
     * @param conversation Ordered list of chat messages (role/content)
     * @param temperature Optional override for response creativity
     * @param maxTokens Optional override for max tokens
     * @return Assistant response text
     */
    public String generateChatResponse(
            String systemPrompt,
            List<Map<String, String>> conversation,
            Double temperature,
            Integer maxTokens) {
        
        if (!aiConfig.isApiKeyConfigured()) {
            throw new RuntimeException("AI_API_KEY is not configured. Cannot generate chat response.");
        }
        
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                messages.add(Map.of("role", "system", "content", systemPrompt));
            }
            if (conversation != null) {
                messages.addAll(conversation);
            }
            if (messages.isEmpty()) {
                throw new IllegalArgumentException("Conversation cannot be empty");
            }
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiConfig.getModel());
            requestBody.put("messages", messages);
            requestBody.put("temperature", temperature != null ? temperature : aiConfig.getTemperature());
            requestBody.put("max_tokens", maxTokens != null ? maxTokens : aiConfig.getMaxTokens());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiConfig.getApiKey());
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                OPENAI_API_URL,
                HttpMethod.POST,
                request,
                String.class
            );
            
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("OpenAI API returned error: " + response.getStatusCode());
            }
            
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode choices = responseJson.get("choices");
            if (choices == null || !choices.isArray() || choices.size() == 0) {
                throw new RuntimeException("OpenAI API returned invalid response format");
            }
            
            JsonNode message = choices.get(0).get("message");
            
            if (message == null || message.get("content") == null) {
                throw new RuntimeException("OpenAI API returned invalid response format");
            }
            
            return message.get("content").asText();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to call OpenAI API: " + e.getMessage(), e);
        }
    }

    /**
     * Indicates if the AI client can be used (API key configured).
     */
    public boolean isEnabled() {
        return aiConfig.isApiKeyConfigured();
    }
}







