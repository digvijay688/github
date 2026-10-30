package com.sahil.ai.resume.analyzer.ai;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class AgenticResumeService {
    private static final int MAX_TEXT_LENGTH = 18000;
    private final ResumeSummaryAgent summaryAgent;
    private final JobMatchAgent jobMatchAgent;
    private final CareerPlanAgent careerPlanAgent;

    public AgenticResumeService(ResumeSummaryAgent summaryAgent,
            JobMatchAgent jobMatchAgent, CareerPlanAgent careerPlanAgent) {
        this.summaryAgent = summaryAgent;
        this.jobMatchAgent = jobMatchAgent;
        this.careerPlanAgent = careerPlanAgent;
    }

    public Map<String, String> analyze(String resumeText, String jobDescription) {
        if (resumeText == null || resumeText.isBlank()) {
            throw new IllegalArgumentException("Resume text is required.");
        }
        if (jobDescription == null || jobDescription.isBlank()) {
            throw new IllegalArgumentException("Job description is required.");
        }
        String safeResume = resumeText.substring(0, Math.min(resumeText.length(), MAX_TEXT_LENGTH));
        String safeJob = jobDescription.substring(0, Math.min(jobDescription.length(), 8000));

        // A simple orchestrator: agents run in a predictable order.
        Map<String, String> result = new LinkedHashMap<>();
        result.put("summary", summaryAgent.run(safeResume));
        result.put("jobMatchAnalysis", jobMatchAgent.run(safeResume, safeJob));
        result.put("careerPlan", careerPlanAgent.run(safeResume, safeJob));
        result.put("notice", "AI-generated advice can be inaccurate. Verify all details before using them.");
        return result;
    }
}
