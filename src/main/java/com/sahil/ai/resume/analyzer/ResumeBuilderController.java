
package com.sahil.ai.resume.analyzer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResumeBuilderController {

    private static final int MAX_PHOTO_SIZE = 2 * 1024 * 1024;

    @PostMapping(
        value = "/resume-builder/pdf",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> createResumePdf(
            @RequestBody Map<String, Object> data) throws IOException {

        byte[] photoBytes = readPhoto(data);

        try (
            PDDocument document = new PDDocument();
            ByteArrayOutputStream output = new ByteArrayOutputStream()
        ) {
            PDType1Font regular = new PDType1Font(
                Standard14Fonts.FontName.HELVETICA
            );

            PDType1Font bold = new PDType1Font(
                Standard14Fonts.FontName.HELVETICA_BOLD
            );

            PDImageXObject photo = null;

            if (photoBytes != null) {
                photo = PDImageXObject.createFromByteArray(
                    document, photoBytes, "profile-photo"
                );
            }

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float left = 50;
            float right = 545;
            float width = right - left;
            float y = 785;

            try (PDPageContentStream stream =
                     new PDPageContentStream(document, page)) {

                // Name
                String name = safeText(
                    text(data, "fullName", "Your Name")
                ).toUpperCase();

                drawText(
                    stream, bold, 20,
                    left, y, name, 300
                );

                // Profile photo
                if (photo != null) {
                    float maxWidth = 85;
                    float maxHeight = 100;

                    float scale = Math.min(
                        maxWidth / photo.getWidth(),
                        maxHeight / photo.getHeight()
                    );

                    float imageWidth = photo.getWidth() * scale;
                    float imageHeight = photo.getHeight() * scale;

                    stream.drawImage(
                        photo,
                        right - imageWidth,
                        y - imageHeight + 8,
                        imageWidth,
                        imageHeight
                    );
                }

                y -= 30;

                y = drawParagraph(
                    stream, regular, 9, left, y, width,
                    join(
                        text(data, "email", ""),
                        text(data, "phone", ""),
                        text(data, "location", "")
                    )
                );

                y = drawParagraph(
                    stream, regular, 9, left, y - 3, width,
                    join(
                        text(data, "linkedin", ""),
                        text(data, "github", "")
                    )
                );

                y -= 15;

                y = drawSection(
                    document, stream, page, bold, regular,
                    "PROFESSIONAL SUMMARY",
                    text(data, "summary", ""),
                    left, y, width
                );

                y = drawSection(
                    document, stream, page, bold, regular,
                    "EDUCATION",
                    join(
                        text(data, "degree", ""),
                        text(data, "college", ""),
                        text(data, "graduation", ""),
                        text(data, "educationDetails", "")
                    ),
                    left, y, width
                );

                y = drawSection(
                    document, stream, page, bold, regular,
                    "TECHNICAL SKILLS",
                    text(data, "skills", ""),
                    left, y, width
                );

                y = drawSection(
                    document, stream, page, bold, regular,
                    "PROJECTS",
                    join(
                        text(data, "projectName", ""),
                        text(data, "projectTech", ""),
                        text(data, "projectDescription", "")
                    ),
                    left, y, width
                );

                drawSection(
                    document, stream, page, bold, regular,
                    "EXPERIENCE / INTERNSHIP",
                    join(
                        text(data, "experienceTitle", ""),
                        text(data, "company", ""),
                        text(data, "duration", ""),
                        text(data, "experienceDescription", "")
                    ),
                    left, y, width
                );
            }

            document.save(output);
            byte[] pdfBytes = output.toByteArray();

            return ResponseEntity.ok()
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"Professional_Resume.pdf\""
                )
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
        }
    }

    private byte[] readPhoto(Map<String, Object> data) {
        String photoData = text(data, "photoData", "");
        String photoMime = text(data, "photoMime", "");

        if (photoData.isBlank()) {
            return null;
        }

        if (!photoMime.equals("image/jpeg")
                && !photoMime.equals("image/png")) {
            throw new IllegalArgumentException(
                "Please upload a JPG or PNG photo."
            );
        }

        String prefix = "data:" + photoMime + ";base64,";

        if (!photoData.startsWith(prefix)) {
            throw new IllegalArgumentException("Invalid photo data.");
        }

        byte[] bytes;

        try {
            bytes = Base64.getDecoder().decode(
                photoData.substring(prefix.length())
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid photo encoding.");
        }

        if (bytes.length == 0 || bytes.length > MAX_PHOTO_SIZE) {
            throw new IllegalArgumentException(
                "Photo must be smaller than 2 MB."
            );
        }

        return bytes;
    }

    private float drawSection(
            PDDocument document,
            PDPageContentStream stream,
            PDPage page,
            PDType1Font bold,
            PDType1Font regular,
            String heading,
            String content,
            float x,
            float y,
            float width) throws IOException {

        if (y < 90) {
            stream.close();
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // This compact version keeps the section renderer simple.
            // If your resume is very long, shorten its content.
            throw new IOException(
                "Resume content is too long for one page. " +
                "Please shorten the details and try again."
            );
        }

        y -= 5;

        drawText(stream, bold, 11, x, y, heading, width);
        y -= 17;

        String actualContent = content.isBlank()
            ? "Not provided" : content;

        y = drawParagraph(
            stream, regular, 9, x, y, width, actualContent
        );

        return y - 13;
    }

    private float drawParagraph(
            PDPageContentStream stream,
            PDType1Font font,
            float fontSize,
            float x,
            float y,
            float maxWidth,
            String content) throws IOException {

        String safe = safeText(content);

        for (String paragraph : safe.split("\\R")) {
            String[] words = paragraph.split("\\s+");
            StringBuilder line = new StringBuilder();

            for (String word : words) {
                String candidate = line.length() == 0
                    ? word : line + " " + word;

                float candidateWidth =
                    font.getStringWidth(candidate) / 1000 * fontSize;

                if (candidateWidth > maxWidth && line.length() > 0) {
                    drawText(stream, font, fontSize, x, y,
                        line.toString(), maxWidth);

                    y -= fontSize + 5;
                    line.setLength(0);
                    line.append(word);
                } else {
                    if (line.length() > 0) {
                        line.append(" ");
                    }
                    line.append(word);
                }
            }

            if (line.length() > 0) {
                drawText(stream, font, fontSize, x, y,
                    line.toString(), maxWidth);
                y -= fontSize + 5;
            }
        }

        return y;
    }

    private void drawText(
            PDPageContentStream stream,
            PDType1Font font,
            float size,
            float x,
            float y,
            String text,
            float maxWidth) throws IOException {

        String safe = safeText(text);

        while (!safe.isEmpty()
                && font.getStringWidth(safe) / 1000 * size > maxWidth) {
            safe = safe.substring(0, safe.length() - 1);
        }

        stream.beginText();
        stream.setFont(font, size);
        stream.setNonStrokingColor(40, 43, 65);
        stream.newLineAtOffset(x, y);
        stream.showText(safe);
        stream.endText();
    }

    private String text(
            Map<String, Object> data,
            String key,
            String fallback) {

        Object value = data.get(key);
        return value == null ? fallback : value.toString().trim();
    }

    private String join(String... values) {
        List<String> parts = new ArrayList<>();

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                parts.add(value.trim());
            }
        }

        return String.join(" | ", parts);
    }

    private String safeText(String value) {
        return value == null ? "" : value
            .replace("\r", "\n")
            .replaceAll("[^\\x20-\\x7E\\n\\t]", "?");
    }
}
