package com.placementgo.backend.feedback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "feedback")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String featureType; // RESUME_DOWNLOAD, AUTO_APPLY, GENERAL, etc.

    @Column(nullable = false)
    private int rating; // 1-5 stars

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    @Builder.Default
    private String status = "NEW"; // NEW, REVIEWED, RESOLVED

    private String userEmail;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
