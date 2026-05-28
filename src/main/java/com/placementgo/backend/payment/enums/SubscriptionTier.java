package com.placementgo.backend.payment.enums;

import lombok.Getter;

@Getter
public enum SubscriptionTier {
    STARTER("Starter", 0, 3, 1, 3),      // Free: 3 resume generations, 1 job search, 3 jobs clickable
    PRO("Pro", 9900, 15, 20, 50),          // ₹99: 15 resumes, 20 searches, 50 applications
    ENTERPRISE("Enterprise", 49900, 50, 60, 200); // ₹499: 50 resumes, 60 searches, 200 applications

    private final String displayName;
    private final int priceInCents;           // Price in INR paise (1 rupee = 100 paise)
    private final int resumeGenerationLimit;  // Resumes per month
    private final int jobSearchLimit;         // Job searches per month
    private final int jobApplicationLimit;    // Job applications per month

    SubscriptionTier(String displayName, int priceInCents, int resumeGenerationLimit, 
                     int jobSearchLimit, int jobApplicationLimit) {
        this.displayName = displayName;
        this.priceInCents = priceInCents;
        this.resumeGenerationLimit = resumeGenerationLimit;
        this.jobSearchLimit = jobSearchLimit;
        this.jobApplicationLimit = jobApplicationLimit;
    }

    public static SubscriptionTier fromString(String plan) {
        if (plan == null) return STARTER;
        return switch (plan.toUpperCase()) {
            case "PRO", "PROFESSIONAL" -> PRO;
            case "ENTERPRISE" -> ENTERPRISE;
            default -> STARTER;
        };
    }

    public boolean isFreeTier() {
        return this == STARTER;
    }
}
