package com.placementgo.backend.dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "application_notes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID applicationId;

    @Column(nullable = false)
    private UUID userId;

    @Column(columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}