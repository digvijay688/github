
package com.sahil.ai.resume.analyzer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ResumeUploadController {

    private final List<String> knownSkills = List.of(
        "Java", "Python", "HTML", "CSS",
        "JavaScript", "SQL", "Spring Boot", "Git"
    );

    @PostMapping(
        value = "/analyze",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Map<String, Object> analyzeResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobDescription") String jobDescription)
            throws IOException {

        if (file.isEmpty()
                || file.getOriginalFilename() == null
                || !file.getOriginalFilename()
                    .toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException(
                "Please upload a valid PDF file."
            );
        }

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {

            String resumeText =
                new PDFTextStripper().getText(document);

            List<String> detectedSkills = knownSkills.stream()
                .filter(skill -> resumeText.toLowerCase(Locale.ROOT)
                    .contains(skill.toLowerCase(Locale.ROOT)))
                .toList();

            List<String> requiredSkills = knownSkills.stream()
                .filter(skill -> jobDescription.toLowerCase(Locale.ROOT)
                    .contains(skill.toLowerCase(Locale.ROOT)))
                .toList();

            List<String> matchedSkills = detectedSkills.stream()
                .filter(requiredSkills::contains)
                .toList();

            // NEW: Find skills missing from the resume
            List<String> missingSkills = requiredSkills.stream()
                .filter(skill -> !matchedSkills.contains(skill))
                .toList();

            int score = Math.min(detectedSkills.size() * 10, 100);

            double matchPercentage = requiredSkills.isEmpty()
                ? 0.0
                : Math.round(
                    matchedSkills.size() * 10000.0
                        / requiredSkills.size()
                  ) / 100.0;

            return Map.of(
                "fileName", file.getOriginalFilename(),
                "detectedSkills", detectedSkills,
                "count", detectedSkills.size(),
                "resumeScore", score,
                "requiredSkills", requiredSkills,
                "matchedSkills", matchedSkills,
                "missingSkills", missingSkills,
                "jobMatchPercentage", matchPercentage,
                "resumeText", resumeText
            );
        }
    }
}
