
package com.sahil.ai.resume.analyzer.ai;

import org.springframework.stereotype.Component;

@Component
public class CareerPlanAgent {

    private final OllamaChatClient ai;

    public CareerPlanAgent(OllamaChatClient ai) {
        this.ai = ai;
    }

    public String run(String resumeText, String jobDescription) {
        return ai.complete(
            "You are a practical career coach. Create a realistic "
            + "beginner-friendly 3-step learning and resume-improvement plan. "
            + "Prioritize job-relevant gaps. Do not tell the user to claim "
            + "skills they do not have.",

            "Based on this resume and target job, create a 3-step plan. "
            + "For each step include what to learn/do and a small "
            + "proof-of-skill project.\n\nRESUME:\n" + resumeText
            + "\n\nJOB DESCRIPTION:\n" + jobDescription
        );
    }
}
