package com.placementgo.backend.resume.dto;

import com.placementgo.backend.resume.model.Resume;
import com.placementgo.backend.resume.model.ResumeStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class ResumeDetailResponse {

    private UUID id;
    private String originalFileName;
    private String jobDescription;
    private String templateName;
    private ResumeStatus status;
    private LocalDateTime createdAt;
    private String pdfBase64;
    private String generatedLatex;

    public ResumeDetailResponse(Resume resume) {
        this.id = resume.getId();
        this.originalFileName = resume.getOriginalFileName();
        this.jobDescription = resume.getJobDescription();
        this.templateName = resume.getTemplateName();
        this.status = resume.getStatus();
        this.createdAt = resume.getCreatedAt();
        this.pdfBase64 = resume.getGeneratedPdfBase64();
        this.generatedLatex = resume.getGeneratedLatex();
    }

    public UUID getId() { return id; }
    public String getOriginalFileName() { return originalFileName; }
    public String getJobDescription() { return jobDescription; }
    public String getTemplateName() { return templateName; }
    public ResumeStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getPdfBase64() { return pdfBase64; }
    public String getGeneratedLatex() { return generatedLatex; }
}
