package com.placementgo.backend.dashboard.service;

import com.placementgo.backend.dashboard.dto.*;
import com.placementgo.backend.dashboard.entity.*;
import com.placementgo.backend.dashboard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationNoteRepository noteRepository;

    @Transactional
    public ApplicationResponse create(CreateApplicationRequest request, UUID userId) {
        log.info("Creating application for user: {}", userId);
        
        Application application = Application.builder()
                .company(request.getCompany())
                .role(request.getRole())
                .jobLink(request.getJobLink())
                .appliedDate(request.getAppliedDate())
                .status(ApplicationStatus.APPLIED)
                .userId(userId)
                .build();

        Application saved = applicationRepository.save(application);
        log.info("Application created with ID: {}", saved.getId());
        
        return mapToResponse(saved);
    }

    public List<ApplicationResponse> getAll(UUID userId) {
        log.info("Fetching all applications for user: {}", userId);
        
        return applicationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ApplicationResponse getById(UUID id, UUID userId) {
        log.info("Fetching application {} for user: {}", id, userId);
        
        Application app = getOwnedApplication(id, userId);
        return mapToResponse(app);
    }

    @Transactional
    public ApplicationResponse update(UUID id, UpdateApplicationRequest request, UUID userId) {
        log.info("Updating application {} for user: {}", id, userId);
        
        Application app = getOwnedApplication(id, userId);

        app.setCompany(request.getCompany());
        app.setRole(request.getRole());
        app.setJobLink(request.getJobLink());
        app.setAppliedDate(request.getAppliedDate());

        Application updated = applicationRepository.save(app);
        log.info("Application {} updated successfully", id);
        
        return mapToResponse(updated);
    }

    @Transactional
    public ApplicationResponse updateStatus(UUID id, UpdateStatusRequest request, UUID userId) {
        log.info("Updating status of application {} to {} for user: {}", id, request.getStatus(), userId);
        
        Application app = getOwnedApplication(id, userId);
        app.setStatus(request.getStatus());

        Application updated = applicationRepository.save(app);
        log.info("Application {} status updated successfully", id);
        
        return mapToResponse(updated);
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        log.info("Deleting application {} for user: {}", id, userId);
        
        Application app = getOwnedApplication(id, userId);
        
        // Delete associated notes first
        noteRepository.deleteByApplicationId(id);
        
        // Delete the application
        applicationRepository.delete(app);
        
        log.info("Application {} deleted successfully", id);
    }

    @Transactional
    public void addNote(UUID applicationId, CreateNoteRequest request, UUID userId) {
        log.info("Adding note to application {} for user: {}", applicationId, userId);
        
        // Verify ownership
        getOwnedApplication(applicationId, userId);

        ApplicationNote note = ApplicationNote.builder()
                .applicationId(applicationId)
                .userId(userId)
                .note(request.getNote())
                .build();

        noteRepository.save(note);
        log.info("Note added to application {} successfully", applicationId);
    }

    public List<ApplicationNote> getNotes(UUID applicationId, UUID userId) {
        log.info("Fetching notes for application {} for user: {}", applicationId, userId);
        
        // Verify ownership
        getOwnedApplication(applicationId, userId);
        
        return noteRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId);
    }

    private Application getOwnedApplication(UUID id, UUID userId) {
        return applicationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> {
                    log.error("Application {} not found or unauthorized for user {}", id, userId);
                    return new RuntimeException("Application not found or unauthorized");
                });
    }

    private ApplicationResponse mapToResponse(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .company(app.getCompany())
                .role(app.getRole())
                .jobLink(app.getJobLink())
                .appliedDate(app.getAppliedDate())
                .status(app.getStatus())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}