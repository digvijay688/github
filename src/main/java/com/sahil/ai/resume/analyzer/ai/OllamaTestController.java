package com.sahil.ai.resume.analyzer.ai;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ollama")
public class OllamaTestController {

    private final OllamaChatClient ollamaChatClient;

    public OllamaTestController(OllamaChatClient ollamaChatClient) {
        this.ollamaChatClient = ollamaChatClient;
    }

    @GetMapping("/test")
    public Map<String, String> testOllama() {
        String answer = ollamaChatClient.complete(
                "You are a helpful AI assistant.",
                "Reply in one short sentence: What can you do for a resume analyzer?"
        );

        return Map.of("status", "success", "response", answer);
    }
}