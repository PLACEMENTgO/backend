package com.placementgo.backend.dashboard.controller;

import com.placementgo.backend.dashboard.dto.*;
import com.placementgo.backend.dashboard.entity.ApplicationNote;
import com.placementgo.backend.dashboard.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ApplicationController {

    private final ApplicationService applicationService;

    private UUID getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        return UUID.fromString(userDetails.getUsername());
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            @Valid @RequestBody CreateApplicationRequest request) {
        ApplicationResponse response = applicationService.create(request, getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {
        List<ApplicationResponse> applications = applicationService.getAll(getCurrentUserId());
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplication(@PathVariable UUID id) {
        ApplicationResponse response = applicationService.getById(id, getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponse> updateApplication(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateApplicationRequest request) {
        ApplicationResponse response = applicationService.update(id, request, getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {
        ApplicationResponse response = applicationService.updateStatus(id, request, getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable UUID id) {
        applicationService.delete(id, getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<Void> addNote(
            @PathVariable UUID id,
            @Valid @RequestBody CreateNoteRequest request) {
        applicationService.addNote(id, request, getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<List<ApplicationNote>> getNotes(@PathVariable UUID id) {
        List<ApplicationNote> notes = applicationService.getNotes(id, getCurrentUserId());
        return ResponseEntity.ok(notes);
    }
}