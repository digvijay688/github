
package com.sahil.ai.resume.analyzer.ai;

import org.springframework.stereotype.Component;

@Component
public class JobMatchAgent {

    private final OllamaChatClient ai;

    public JobMatchAgent(OllamaChatClient ai) {
        this.ai = ai;
    }

    public String run(String resumeText, String jobDescription) {
        return ai.complete(
            "You are a job-match analyst. Compare evidence in the resume "
            + "with the job description. Do not assume a skill exists unless "
            + "the resume supports it. Use clear headings: Strong matches, "
            + "Gaps, Evidence to clarify. Do not make hiring decisions.",

            "RESUME:\n" + resumeText
            + "\n\nJOB DESCRIPTION:\n" + jobDescription
        );
    }
}
