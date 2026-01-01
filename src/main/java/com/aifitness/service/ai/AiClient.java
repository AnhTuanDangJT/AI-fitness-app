package com.aifitness.service.ai;

import com.aifitness.ai.AiConfig;
import com.aifitness.exception.AiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared AI client that talks to GitHub Models using the configured GITHUB_TOKEN.
 */
@Component
public class AiClient {

    private static final Logger logger = LoggerFactory.getLogger(AiClient.class);
    private static final String GITHUB_MODELS_URL = "https://api.githubcopilot.com/chat/completions";

    private final AiConfig aiConfig;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public AiClient(AiConfig aiConfig) {
        this.aiConfig = aiConfig;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = buildRestTemplate();
    }

    /**
     * Generates an AI response using the shared GitHub client.
     *
     * @param systemPrompt Behavior + policy instructions
     * @param userPrompt   User intent prompt
     * @param context      Additional structured context that will be stringified
     * @return Raw assistant response text
     */
    public String generateAIResponse(String systemPrompt, String userPrompt, Map<String, Object> context) {
        if (!StringUtils.hasText(systemPrompt)) {
            throw new IllegalArgumentException("systemPrompt cannot be empty");
        }
        if (!StringUtils.hasText(userPrompt)) {
            throw new IllegalArgumentException("userPrompt cannot be empty");
        }

        String token = aiConfig.getGithubToken();
        if (!StringUtils.hasText(token)) {
            throw new AiServiceException("GITHUB_TOKEN is not configured");
        }

        try {
            String contextBlock = buildContextBlock(context);
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", contextBlock + userPrompt));

            Map<String, Object> body = new HashMap<>();
            body.put("model", aiConfig.getGithubModel());
            body.put("messages", messages);
            body.put("max_tokens", aiConfig.getMaxTokens());
            body.put("temperature", Math.min(Math.max(aiConfig.getTemperature(), 0.0), 1.2));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setBearerAuth(token);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(GITHUB_MODELS_URL, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new AiServiceException("AI provider returned unexpected status: " + response.getStatusCode());
            }

            return extractContent(response.getBody());
        } catch (ResourceAccessException ex) {
            logger.error("AI request timeout", ex);
            throw new AiServiceException("AI request timed out", ex);
        } catch (HttpStatusCodeException ex) {
            logger.error("AI provider error: {}", ex.getResponseBodyAsString(), ex);
            throw new AiServiceException("AI provider error: " + ex.getStatusCode(), ex);
        } catch (AiServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Failed to call AI provider", ex);
            throw new AiServiceException("Failed to call AI provider: " + ex.getMessage(), ex);
        }
    }

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(25).toMillis());
        return new RestTemplate(factory);
    }

    private String buildContextBlock(Map<String, Object> context) {
        if (context == null || context.isEmpty()) {
            return "";
        }
        try {
            String contextJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(context);
            return "Context:\n" + contextJson + "\n\n";
        } catch (Exception ex) {
            logger.warn("Failed to serialize AI context, continuing without detailed context", ex);
            return "";
        }
    }

    private String extractContent(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new AiServiceException("AI provider returned empty choices");
        }
        JsonNode message = choices.get(0).path("message");
        String content = message.path("content").asText();
        if (!StringUtils.hasText(content)) {
            throw new AiServiceException("AI provider returned empty response");
        }
        return content.trim();
    }
}

