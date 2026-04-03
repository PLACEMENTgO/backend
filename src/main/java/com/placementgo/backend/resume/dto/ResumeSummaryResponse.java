package com.placementgo.backend.resume.dto;

import com.placementgo.backend.resume.model.Resume;
import com.placementgo.backend.resume.model.ResumeStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class ResumeSummaryResponse {

    private UUID id;
    private String originalFileName;
    private String jobDescriptionSnippet;
    private String templateName;
    private ResumeStatus status;
    private LocalDateTime createdAt;

    public ResumeSummaryResponse(Resume resume) {
        this.id = resume.getId();
        this.originalFileName = resume.getOriginalFileName();
        this.templateName = resume.getTemplateName();
        this.status = resume.getStatus();
        this.createdAt = resume.getCreatedAt();
        String jd = resume.getJobDescription();
        this.jobDescriptionSnippet = (jd != null && jd.length() > 150)
                ? jd.substring(0, 150) + "..."
                : jd;
    }

    public UUID getId() { return id; }
    public String getOriginalFileName() { return originalFileName; }
    public String getJobDescriptionSnippet() { return jobDescriptionSnippet; }
    public String getTemplateName() { return templateName; }
    public ResumeStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
