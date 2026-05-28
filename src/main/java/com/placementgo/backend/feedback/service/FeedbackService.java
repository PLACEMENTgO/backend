package com.placementgo.backend.feedback.service;

import com.placementgo.backend.auth.model.User;
import com.placementgo.backend.auth.repository.UserRepository;
import com.placementgo.backend.feedback.dto.FeedbackResponse;
import com.placementgo.backend.feedback.dto.SubmitFeedbackRequest;
import com.placementgo.backend.feedback.entity.Feedback;
import com.placementgo.backend.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackResponse submitFeedback(UUID userId, SubmitFeedbackRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Feedback feedback = Feedback.builder()
                .userId(userId)
                .userEmail(user.getEmail())
                .featureType(request.getFeatureType())
                .rating(request.getRating())
                .comment(request.getComment())
                .status("NEW")
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        log.info("Feedback submitted by user {} for {}", userId, request.getFeatureType());

        return toResponse(saved);
    }

    public Page<FeedbackResponse> getAllFeedback(Pageable pageable) {
        return feedbackRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    public List<FeedbackResponse> getUserFeedback(UUID userId) {
        return feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public FeedbackResponse updateStatus(UUID feedbackId, String newStatus) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));
        
        feedback.setStatus(newStatus);
        Feedback updated = feedbackRepository.save(feedback);
        log.info("Feedback {} status updated to {}", feedbackId, newStatus);
        
        return toResponse(updated);
    }

    private FeedbackResponse toResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .userId(feedback.getUserId())
                .userEmail(feedback.getUserEmail())
                .featureType(feedback.getFeatureType())
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .status(feedback.getStatus())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
