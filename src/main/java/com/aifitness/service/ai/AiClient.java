package com.aifitness.service.ai;

import com.aifitness.exception.AiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Shared AI client that talks to GitHub Models using the configured GITHUB_TOKEN.
 */
@Component
public class AiClient {

    private static final Logger logger = LoggerFactory.getLogger(AiClient.class);

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

        logger.info("Rule-based AI mode active (no LLM)");
        throw new AiServiceException("Rule-based AI mode active (no LLM)");
    }
}