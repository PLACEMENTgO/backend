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

    @Column(columnDefinition = "TEXT")
    private String parsedJson;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    private String templateName;

    @Column(columnDefinition = "TEXT")
    private String generatedLatex;

    @Column(columnDefinition = "TEXT")
    private String generatedPdfBase64;

    private LocalDateTime createdAt;
}
