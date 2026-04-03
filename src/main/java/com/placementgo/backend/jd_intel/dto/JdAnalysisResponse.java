package com.placementgo.backend.jd_intel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JdAnalysisResponse {
    private String company;
    private String role;
    private List<String> technicalQuestions;
    private List<String> behavioralQuestions;
    private List<String> codingFocus;
    private List<String> systemDesignFocus;
    private List<String> predictedRounds;
    private String difficultyLevel;
    private List<String> rejectionReasons;
    private List<String> companyTips;

    // Fields expected by the frontend
    private String sourceSummary;
    private String confidenceScore;
    private List<String> focusAreas;
    private List<EvaluationCriteria> evaluationCriteria;
    private List<PreparationItem> preparationChecklist;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvaluationCriteria {
        private String name;
        private int percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreparationItem {
        private int priority;
        private String title;
        private String description;
    }
}
