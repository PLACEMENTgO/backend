package com.placementgo.backend.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TierLimitsResponse {
    private String tier;
    private String displayName;
    private int priceInCents;
    private int resumeGenerationLimit;
    private int jobSearchLimit;
    private int jobApplicationLimit;
    private int resumeGenerationsUsed;
    private int jobSearchesUsed;
    private int jobApplicationsUsed;
}
