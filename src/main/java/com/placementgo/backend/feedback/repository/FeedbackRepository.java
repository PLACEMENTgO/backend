package com.placementgo.backend.feedback.repository;

import com.placementgo.backend.feedback.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {
    
    Page<Feedback> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    List<Feedback> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    long countByStatus(String status);
    
}
