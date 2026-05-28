package com.placementgo.backend.feedback.controller;

import com.placementgo.backend.feedback.dto.FeedbackResponse;
import com.placementgo.backend.feedback.dto.SubmitFeedbackRequest;
import com.placementgo.backend.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackResponse> submitFeedback(
            @RequestHeader("X-User-ID") String userIdHeader,
            @RequestBody SubmitFeedbackRequest request) {
        UUID userId = UUID.fromString(userIdHeader);
        FeedbackResponse response = feedbackService.submitFeedback(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<FeedbackResponse>> getMyFeedback(
            @RequestHeader("X-User-ID") String userIdHeader) {
        UUID userId = UUID.fromString(userIdHeader);
        List<FeedbackResponse> feedback = feedbackService.getUserFeedback(userId);
        return ResponseEntity.ok(feedback);
    }
}
