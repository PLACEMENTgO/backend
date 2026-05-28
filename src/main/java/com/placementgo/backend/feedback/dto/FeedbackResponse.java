package com.placementgo.backend.feedback.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class FeedbackResponse {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private String featureType;
    private int rating;
    private String comment;
    private String status;
    private Instant createdAt;
}
