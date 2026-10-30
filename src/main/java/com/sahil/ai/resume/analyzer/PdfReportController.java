
package com.sahil.ai.resume.analyzer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PdfReportController {

    private static final Map<String, String> SUGGESTIONS = Map.of(
        "Java", "Practice OOP, collections and exception handling.",
        "Python", "Learn functions, lists and dictionaries.",
        "HTML", "Practice semantic HTML and forms.",
        "CSS", "Learn Flexbox, Grid and responsive design.",
        "JavaScript", "Practice DOM manipulation and events.",
        "SQL", "Practice SELECT, JOIN and GROUP BY.",
        "Spring Boot", "Build REST APIs and learn dependency injection.",
        "Git", "Practice git add, git commit, git push and branches."
    );

    @PostMapping(
        value = "/analyze/report",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> downloadReport(
            @RequestBody Map<String, Object> data) throws IOException {

        byte[] pdfBytes;

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            List<String> lines = new ArrayList<>();

            lines.add("AI RESUME ANALYSIS REPORT");
            lines.add("=========================");
            lines.add("");
            lines.add("Resume File: " +
                value(data, "fileName", "Resume"));
            lines.add("Resume Score: " +
                value(data, "resumeScore", "0") + " / 100");
            lines.add("Job Match: " +
                value(data, "jobMatchPercentage", "0") + "%");
            lines.add("");

            addSection(lines, "DETECTED SKILLS",
                data.get("detectedSkills"));
            addSection(lines, "MATCHED SKILLS",
                data.get("matchedSkills"));
            addSection(lines, "MISSING SKILLS",
                data.get("missingSkills"));

            lines.add("SKILL IMPROVEMENT SUGGESTIONS");
            List<String> missingSkills = getList(data.get("missingSkills"));

            if (missingSkills.isEmpty()) {
                lines.add("No missing skills for this job.");
            } else {
                for (String skill : missingSkills) {
                    lines.add("- " + skill + ": " +
                        SUGGESTIONS.getOrDefault(
                            skill,
                            "Practice this skill with a small project."
                        ));
                }
            }

            // Standard PDF font supports basic English text.
            PDType1Font regular =
                new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font bold =
                new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            int lineIndex = 0;

            while (lineIndex < lines.size()) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream stream =
                         new PDPageContentStream(document, page)) {

                    float y = 790;

                    while (lineIndex < lines.size() && y > 50) {
                        String line = safeText(lines.get(lineIndex));

                        stream.beginText();
                        stream.setFont(
                            lineIndex == 0 ? bold : regular,
                            lineIndex == 0 ? 18 : 11
                        );
                        stream.newLineAtOffset(50, y);
                        stream.showText(line);
                        stream.endText();

                        y -= line.isEmpty() ? 10 : 18;
                        lineIndex++;
                    }
                }
            }

            document.save(output);
            pdfBytes = output.toByteArray();
        }

        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"Resume_Analysis_Report.pdf\""
            )
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfBytes.length)
            .body(pdfBytes);
    }

    private void addSection(
            List<String> lines, String title, Object value) {

        lines.add(title);

        List<String> items = getList(value);

        if (items.isEmpty()) {
            lines.add("None");
        } else {
            lines.add(String.join(", ", items));
        }

        lines.add("");
    }

    private List<String> getList(Object value) {
        List<String> items = new ArrayList<>();

        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item != null) {
                    items.add(item.toString());
                }
            }
        }

        return items;
    }

    private String value(
            Map<String, Object> data, String key, String fallback) {

        Object value = data.get(key);
        return value == null ? fallback : value.toString();
    }

    private String safeText(String text) {
        // Standard Helvetica font cannot display every Unicode character.
        return text.replaceAll("[^\\x20-\\x7E]", "?");
    }
}
