package com.placementgo.backend.dashboard.repository;

import com.placementgo.backend.dashboard.entity.ApplicationNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationNoteRepository extends JpaRepository<ApplicationNote, UUID> {
    
    List<ApplicationNote> findByApplicationIdOrderByCreatedAtDesc(UUID applicationId);
    
    void deleteByApplicationId(UUID applicationId);
}