package com.placementgo.backend.referral.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "referral_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralRequest {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID userId;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    private String company;

    private String role;

    private String linkedinSearchLink;

    private String token;

    private Instant createdAt;

}