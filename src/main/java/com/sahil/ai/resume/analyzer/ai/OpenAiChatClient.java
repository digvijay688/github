package com.sahil.ai.resume.analyzer.ai;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Small OpenAI-compatible client. The API key is read only from an environment variable. */
@Component
public class OpenAiChatClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public OpenAiChatClient(
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.model:gpt-4.1-mini}") String model) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        this.restClient = RestClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @SuppressWarnings("unchecked")
    public String complete(String systemPrompt, String userPrompt) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException(
                "OpenAI API key is not configured. Set the OPENAI_API_KEY environment variable and restart the app.");
        }

        Map<String, Object> request = Map.of(
            "model", model,
            "temperature", 0.3,
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            )
        );

        Map<String, Object> response = restClient.post()
            .uri("/chat/completions")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .body(request)
            .retrieve()
            .body(Map.class);

        if (response == null || !(response.get("choices") instanceof List<?> choices)
                || choices.isEmpty() || !(choices.get(0) instanceof Map<?, ?> choice)
                || !(choice.get("message") instanceof Map<?, ?> message)
                || message.get("content") == null) {
            throw new IllegalStateException("AI provider returned an unexpected response.");
        }
        return message.get("content").toString().trim();
    }
}
