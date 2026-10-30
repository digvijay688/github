package com.sahil.ai.resume.analyzer;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SkillsController {

    @GetMapping("/skills")
    public Map<String, Object> analyzeSkills(
            @RequestParam String skills) {

        List<String> knownSkills = List.of(
            "Java", "Python", "HTML",
            "CSS", "JavaScript", "SQL",
            "Spring Boot"
        );

        List<String> detectedSkills = knownSkills.stream()
            .filter(skill -> skills.toLowerCase()
                .contains(skill.toLowerCase()))
            .toList();

        return Map.of(
            "input", skills,
            "detectedSkills", detectedSkills,
            "count", detectedSkills.size()
        );
    }
}
