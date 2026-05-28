package com.placementgo.backend.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStatsResponse {
    private long totalUsers;
    private long activeSubscriptions;
    private long starterUsers;
    private long proUsers;
    private long enterpriseUsers;
    private long totalFeedback;
    private long pendingFeedback;
    private double averageRating;
    private UsageStats currentMonthUsage;
    
    @Data
    @Builder
    public static class UsageStats {
        private long totalResumeGenerations;
        private long totalJobSearches;
        private long totalJobApplications;
    }
}
