package com.sahil.ai.resume.analyzer.ai;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/agentic")
public class AgenticResumeController {
    private final AgenticResumeService service;
    public AgenticResumeController(AgenticResumeService service) { this.service = service; }

    @PostMapping("/analyze")
    public Map<String, String> analyze(@RequestBody Map<String, String> request) {
        try {
            return service.analyze(request.get("resumeText"), request.get("jobDescription"));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        } catch (org.springframework.web.client.RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "AI provider request failed. Check the API key, billing/usage limits, model name, and internet connection.");
        }
    }
}
