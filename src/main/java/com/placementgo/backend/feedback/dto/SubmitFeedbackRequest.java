package com.placementgo.backend.feedback.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitFeedbackRequest {
    private String featureType; // RESUME_DOWNLOAD, AUTO_APPLY, GENERAL
    private int rating;         // 1-5 stars
    private String comment;
}
