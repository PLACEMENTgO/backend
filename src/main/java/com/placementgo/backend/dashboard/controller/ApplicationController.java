package com.placementgo.backend.dashboard.controller;

import com.placementgo.backend.dashboard.dto.*;
import com.placementgo.backend.dashboard.entity.ApplicationNote;
import com.placementgo.backend.dashboard.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            @Valid @RequestBody CreateApplicationRequest request,
            @AuthenticationPrincipal UUID userId) {
        
        ApplicationResponse response = applicationService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAllApplications(
            @AuthenticationPrincipal UUID userId) {
        
        List<ApplicationResponse> applications = applicationService.getAll(userId);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplication(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId) {
        
        ApplicationResponse response = applicationService.getById(id, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponse> updateApplication(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateApplicationRequest request,
            @AuthenticationPrincipal UUID userId) {
        
        ApplicationResponse response = applicationService.update(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal UUID userId) {
        
        ApplicationResponse response = applicationService.updateStatus(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId) {
        
        applicationService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<Void> addNote(
            @PathVariable UUID id,
            @Valid @RequestBody CreateNoteRequest request,
            @AuthenticationPrincipal UUID userId) {
        
        applicationService.addNote(id, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<List<ApplicationNote>> getNotes(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId) {
        
        List<ApplicationNote> notes = applicationService.getNotes(id, userId);
        return ResponseEntity.ok(notes);
    }

}