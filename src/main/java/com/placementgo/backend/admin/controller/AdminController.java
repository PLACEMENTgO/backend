package com.placementgo.backend.admin.controller;

import com.placementgo.backend.admin.dto.AdminLoginRequest;
import com.placementgo.backend.admin.dto.AdminLoginResponse;
import com.placementgo.backend.admin.dto.AdminStatsResponse;
import com.placementgo.backend.admin.service.AdminAuthService;
import com.placementgo.backend.admin.service.AdminService;
import com.placementgo.backend.feedback.dto.FeedbackResponse;
import com.placementgo.backend.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;
    private final FeedbackService feedbackService;
    private final AdminAuthService adminAuthService;

    /**
     * Admin login endpoint - generates JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminLoginRequest request) {
        if (!adminAuthService.validateCredentials(request.getUsername(), request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }

        String token = adminAuthService.generateToken(request.getUsername());
        AdminLoginResponse response = new AdminLoginResponse(
                token,
                request.getUsername(),
                adminAuthService.getJwtExpiration()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get admin dashboard statistics - PROTECTED
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getAdminStats() {
        AdminStatsResponse stats = adminService.getAdminStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get all feedback with pagination - PROTECTED
     */
    @GetMapping("/feedback")
    public ResponseEntity<Page<FeedbackResponse>> getAllFeedback(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<FeedbackResponse> feedback = feedbackService.getAllFeedback(PageRequest.of(page, size));
        return ResponseEntity.ok(feedback);
    }

    /**
     * Update feedback status - PROTECTED
     */
    @PutMapping("/feedback/{feedbackId}/status")
    public ResponseEntity<FeedbackResponse> updateFeedbackStatus(
            @PathVariable UUID feedbackId,
            @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        FeedbackResponse updated = feedbackService.updateStatus(feedbackId, newStatus);
        return ResponseEntity.ok(updated);
    }
}
