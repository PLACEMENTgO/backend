package com.placementgo.backend.resume.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Resume {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    private String originalFileName;

    private String storedFilePath;

    @Enumerated(EnumType.STRING)
    private ResumeStatus status;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String parsedJson;

    private LocalDateTime createdAt;
}
