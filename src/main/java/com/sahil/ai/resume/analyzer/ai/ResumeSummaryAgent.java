
package com.sahil.ai.resume.analyzer.ai;

import org.springframework.stereotype.Component;

@Component
public class ResumeSummaryAgent {

    private final OllamaChatClient ai;

    public ResumeSummaryAgent(OllamaChatClient ai) {
        this.ai = ai;
    }

    public String run(String resumeText) {
        return ai.complete(
            "You are a careful resume-writing assistant. "
            + "Use only facts present in the resume. "
            + "Never invent qualifications, employers, dates, or achievements. "
            + "Return a concise professional summary.",

            "Write a professional resume summary in 80-120 words "
            + "based only on this resume text:\n\n" + resumeText
        );
    }
}
