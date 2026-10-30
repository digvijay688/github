
package com.sahil.ai.resume.analyzer.ai;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OllamaChatClient {

    private final RestClient restClient;
    private final String model;

    public OllamaChatClient(
            @Value("${ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${ollama.model:llama3.2:3b}") String model) {

        this.model = model;

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(
                        "Content-Type",
                        MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @SuppressWarnings("unchecked")
    public String complete(String systemPrompt, String userPrompt) {

        Map<String, Object> request = Map.of(
                "model", model,
                "stream", false,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", systemPrompt),
                        Map.of(
                                "role", "user",
                                "content", userPrompt)
                )
        );

        Map<String, Object> response = restClient.post()
                .uri("/api/chat")
                .body(request)
                .retrieve()
                .body(Map.class);

        if (response == null
                || !(response.get("message") instanceof Map<?, ?> message)
                || message.get("content") == null) {
            throw new IllegalStateException(
                    "Ollama returned an unexpected response.");
        }

        return message.get("content").toString().trim();
    }
}
