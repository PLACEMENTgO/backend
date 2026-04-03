package com.placementgo.backend.jd_intel.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placementgo.backend.jd_intel.ai.GeminiClient;
import com.placementgo.backend.jd_intel.dto.JdAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LlmExtractionService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public JdAnalysisResponse extractInsights(String aggregatedText, String company, String role,
            String jobDescription) {
        log.info("Sending text to Gemini for extraction...");

        String prompt = createPrompt(aggregatedText, company, role, jobDescription);

        try {
            String content = geminiClient.generateContent(prompt);

            if (content == null || content.isEmpty()) {
                log.warn("Received empty response from Gemini.");
                return JdAnalysisResponse.builder().build();
            }

            // Cleanup Markdown code blocks if present
            content = content.trim();
            if (content.startsWith("```json")) {
                content = content.substring(7);
            } else if (content.startsWith("```")) {
                content = content.substring(3);
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - 3);
            }
            content = content.trim();

            return objectMapper.readValue(content, JdAnalysisResponse.class);

        } catch (Exception e) {
            log.error("Error during LLM extraction: {}", e.getMessage());
        }

        return JdAnalysisResponse.builder().build();
    }

    private String createPrompt(String text, String company, String role, String jobDescription) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "You are an expert technical recruiter. Analyze the following interview experiences and job description to extract structured interview insights for ");
        sb.append(company).append(" - ").append(role).append(".\n\n");

        if (jobDescription != null && !jobDescription.isEmpty()) {
            sb.append("Job Description:\n").append(jobDescription).append("\n\n");
        }

        // Truncate text to avoid token limits. Gemini Flash has large context window
        // (1M tokens),
        // but let's be safe and efficient.
        int maxChars = 30000;
        String safeText = text.length() > maxChars ? text.substring(0, maxChars) : text;

        sb.append("Interview Experiences Text:\n").append(safeText).append("\n\n");

        sb.append(
                "Extract the following fields in strict JSON format. Do not use markdown (example ```json). Just return the raw JSON:\n");
        sb.append("{\n");
        sb.append("  \"company\": \"").append(company).append("\",\n");
        sb.append("  \"role\": \"").append(role).append("\",\n");
        sb.append("  \"technicalQuestions\": [\"question 1\", \"question 2\"],\n");
        sb.append("  \"behavioralQuestions\": [\"question 1\"],\n");
        sb.append("  \"codingFocus\": [\"topic 1\", \"topic 2\"],\n");
        sb.append("  \"systemDesignFocus\": [\"topic 1\"],\n");
        sb.append("  \"predictedRounds\": [\"Round 1: OA\", \"Round 2: DSA\"],\n");
        sb.append("  \"difficultyLevel\": \"Easy/Medium/Hard\",\n");
        sb.append("  \"rejectionReasons\": [\"reason 1\"],\n");
        sb.append("  \"companyTips\": [\"tip 1\"],\n");
        sb.append("  \"sourceSummary\": \"A brief 1-2 sentence summary of the analysis sources and key findings\",\n");
        sb.append("  \"confidenceScore\": \"85\",\n");
        sb.append("  \"focusAreas\": [\"Data Structures\", \"System Design\", \"Behavioral\"],\n");
        sb.append("  \"evaluationCriteria\": [{\"name\": \"Technical Skills\", \"percentage\": 40}, {\"name\": \"Problem Solving\", \"percentage\": 30}],\n");
        sb.append("  \"preparationChecklist\": [{\"priority\": 1, \"title\": \"Master DSA\", \"description\": \"Practice top problems\"}, {\"priority\": 2, \"title\": \"System Design\", \"description\": \"Study distributed systems\"}]\n");
        sb.append("}\n");

        return sb.toString();
    }
}
