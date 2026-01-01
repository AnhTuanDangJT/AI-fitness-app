package com.aifitness.service.ai;

import java.util.Collections;
import java.util.Map;

/**
 * Simple value object representing a prompt request for the shared AI client.
 */
public class AiPromptPayload {

    private final String systemPrompt;
    private final String userPrompt;
    private final Map<String, Object> context;

    public AiPromptPayload(String systemPrompt, String userPrompt, Map<String, Object> context) {
        this.systemPrompt = systemPrompt;
        this.userPrompt = userPrompt;
        this.context = context != null ? Collections.unmodifiableMap(context) : Collections.emptyMap();
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public String getUserPrompt() {
        return userPrompt;
    }

    public Map<String, Object> getContext() {
        return context;
    }
}

