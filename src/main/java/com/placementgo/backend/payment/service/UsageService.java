package com.placementgo.backend.payment.service;

import com.placementgo.backend.payment.dto.TierLimitsResponse;
import com.placementgo.backend.payment.entity.UsageTracking;
import com.placementgo.backend.payment.enums.SubscriptionTier;
import com.placementgo.backend.payment.repository.UsageTrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsageService {

    private final UsageTrackingRepository usageTrackingRepository;
    private final PaymentService paymentService;

    /**
     * Get tier limits with current usage
     */
    public TierLimitsResponse getTierLimits(UUID userId) {
        SubscriptionTier tier = getUserTier(userId);
        UsageTracking usage = getOrCreateUsageTracking(userId);
        
        return TierLimitsResponse.builder()
                .tier(tier.name())
                .displayName(tier.getDisplayName())
                .priceInCents(tier.getPriceInCents())
                .resumeGenerationLimit(tier.getResumeGenerationLimit())
                .jobSearchLimit(tier.getJobSearchLimit())
                .jobApplicationLimit(tier.getJobApplicationLimit())
                .resumeGenerationsUsed(usage.getResumeGenerationsUsed())
                .jobSearchesUsed(usage.getJobSearchesUsed())
                .jobApplicationsUsed(usage.getJobApplicationsUsed())
                .build();
    }

    /**
     * Get or create usage tracking for current month
     */
    @Transactional
    public UsageTracking getOrCreateUsageTracking(UUID userId) {
        String currentMonth = UsageTracking.getCurrentMonth();
        return usageTrackingRepository.findByUserIdAndTrackingMonth(userId, currentMonth)
                .orElseGet(() -> {
                    UsageTracking newTracking = UsageTracking.builder()
                            .userId(userId)
                            .trackingMonth(currentMonth)
                            .build();
                    return usageTrackingRepository.save(newTracking);
                });
    }

    /**
     * Check if user can generate resume and increment counter if allowed
     */
    @Transactional
    public boolean canGenerateResume(UUID userId) {
        SubscriptionTier tier = getUserTier(userId);
        UsageTracking usage = getOrCreateUsageTracking(userId);
        
        if (usage.getResumeGenerationsUsed() >= tier.getResumeGenerationLimit()) {
            log.warn("User {} exceeded resume generation limit ({}/{})", 
                     userId, usage.getResumeGenerationsUsed(), tier.getResumeGenerationLimit());
            return false;
        }
        
        usage.incrementResumeGeneration();
        usageTrackingRepository.save(usage);
        log.info("User {} generated resume ({}/{})", 
                 userId, usage.getResumeGenerationsUsed(), tier.getResumeGenerationLimit());
        return true;
    }

    /**
     * Check if user can perform job search and increment counter if allowed
     */
    @Transactional
    public boolean canPerformJobSearch(UUID userId) {
        SubscriptionTier tier = getUserTier(userId);
        UsageTracking usage = getOrCreateUsageTracking(userId);
        
        if (usage.getJobSearchesUsed() >= tier.getJobSearchLimit()) {
            log.warn("User {} exceeded job search limit ({}/{})", 
                     userId, usage.getJobSearchesUsed(), tier.getJobSearchLimit());
            return false;
        }
        
        usage.incrementJobSearch();
        usageTrackingRepository.save(usage);
        log.info("User {} performed job search ({}/{})", 
                 userId, usage.getJobSearchesUsed(), tier.getJobSearchLimit());
        return true;
    }

    /**
     * Check if user can apply to job and increment counter if allowed
     */
    @Transactional
    public boolean canApplyToJob(UUID userId) {
        SubscriptionTier tier = getUserTier(userId);
        UsageTracking usage = getOrCreateUsageTracking(userId);
        
        if (usage.getJobApplicationsUsed() >= tier.getJobApplicationLimit()) {
            log.warn("User {} exceeded job application limit ({}/{})", 
                     userId, usage.getJobApplicationsUsed(), tier.getJobApplicationLimit());
            return false;
        }
        
        usage.incrementJobApplication();
        usageTrackingRepository.save(usage);
        log.info("User {} applied to job ({}/{})", 
                 userId, usage.getJobApplicationsUsed(), tier.getJobApplicationLimit());
        return true;
    }

    /**
     * Get current usage stats for a user
     */
    public UsageTracking getCurrentUsage(UUID userId) {
        return getOrCreateUsageTracking(userId);
    }

    /**
     * Get user's subscription tier
     */
    private SubscriptionTier getUserTier(UUID userId) {
        var status = paymentService.getStatus(userId);
        return SubscriptionTier.fromString(status.getPlan());
    }
}
