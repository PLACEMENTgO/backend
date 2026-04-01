package com.placementgo.backend.dashboard.repository;

import com.placementgo.backend.dashboard.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    
    List<Application> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    Optional<Application> findByIdAndUserId(UUID id, UUID userId);
    
    long countByUserId(UUID userId);
}