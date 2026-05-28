package com.placementgo.backend.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "usage_tracking", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "trackingMonth"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    /** Format: YYYY-MM (e.g., "2026-05") */
    @Column(nullable = false, length = 7)
    private String trackingMonth;

    @Builder.Default
    @Column(nullable = false)
    private int resumeGenerationsUsed = 0;

    @Builder.Default
    @Column(nullable = false)
    private int jobSearchesUsed = 0;

    @Builder.Default
    @Column(nullable = false)
    private int jobApplicationsUsed = 0;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Get current month in YYYY-MM format
     */
    public static String getCurrentMonth() {
        return YearMonth.now(ZoneOffset.UTC).toString();
    }

    public void incrementResumeGeneration() {
        this.resumeGenerationsUsed++;
    }

    public void incrementJobSearch() {
        this.jobSearchesUsed++;
    }

    public void incrementJobApplication() {
        this.jobApplicationsUsed++;
    }
}
