package com.placementgo.backend.admin.service;

import com.placementgo.backend.admin.dto.AdminStatsResponse;
import com.placementgo.backend.auth.repository.UserRepository;
import com.placementgo.backend.feedback.repository.FeedbackRepository;
import com.placementgo.backend.payment.entity.UsageTracking;
import com.placementgo.backend.payment.repository.SubscriptionRepository;
import com.placementgo.backend.payment.repository.UsageTrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FeedbackRepository feedbackRepository;
    private final UsageTrackingRepository usageTrackingRepository;

    public AdminStatsResponse getAdminStats() {
        long totalUsers = userRepository.count();
        
        // Count active subscriptions (ACTIVE and not expired)
        long activeSubscriptions = subscriptionRepository.findAll().stream()
                .filter(sub -> "ACTIVE".equals(sub.getStatus()) 
                        && (sub.getExpiresAt() == null || sub.getExpiresAt().isAfter(Instant.now())))
                .count();
        
        // Count subscriptions by plan (active only)
        long proUsers = subscriptionRepository.findAll().stream()
                .filter(sub -> "ACTIVE".equals(sub.getStatus()) 
                        && "PRO".equals(sub.getPlan())
                        && (sub.getExpiresAt() == null || sub.getExpiresAt().isAfter(Instant.now())))
                .count();
        
        long enterpriseUsers = subscriptionRepository.findAll().stream()
                .filter(sub -> "ACTIVE".equals(sub.getStatus()) 
                        && "ENTERPRISE".equals(sub.getPlan())
                        && (sub.getExpiresAt() == null || sub.getExpiresAt().isAfter(Instant.now())))
                .count();
        
        long starterUsers = totalUsers - proUsers - enterpriseUsers;
        
        // Feedback stats
        long totalFeedback = feedbackRepository.count();
        long pendingFeedback = feedbackRepository.countByStatus("NEW");
        
        // Calculate average rating
        List<Integer> allRatings = feedbackRepository.findAll().stream()
                .map(f -> f.getRating())
                .toList();
        double averageRating = allRatings.isEmpty() ? 0.0 : 
                allRatings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        
        // Current month usage stats
        String currentMonth = UsageTracking.getCurrentMonth();
        List<UsageTracking> currentMonthUsage = usageTrackingRepository.findAll().stream()
                .filter(u -> currentMonth.equals(u.getTrackingMonth()))
                .toList();
        
        long totalResumes = currentMonthUsage.stream()
                .mapToInt(UsageTracking::getResumeGenerationsUsed).sum();
        long totalSearches = currentMonthUsage.stream()
                .mapToInt(UsageTracking::getJobSearchesUsed).sum();
        long totalApplications = currentMonthUsage.stream()
                .mapToInt(UsageTracking::getJobApplicationsUsed).sum();
        
        AdminStatsResponse.UsageStats usageStats = AdminStatsResponse.UsageStats.builder()
                .totalResumeGenerations(totalResumes)
                .totalJobSearches(totalSearches)
                .totalJobApplications(totalApplications)
                .build();
        
        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeSubscriptions(activeSubscriptions)
                .starterUsers(starterUsers)
                .proUsers(proUsers)
                .enterpriseUsers(enterpriseUsers)
                .totalFeedback(totalFeedback)
                .pendingFeedback(pendingFeedback)
                .averageRating(Math.round(averageRating * 10.0) / 10.0)
                .currentMonthUsage(usageStats)
                .build();
    }
}
