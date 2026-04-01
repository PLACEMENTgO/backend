package com.placementgo.backend.dashboard.dto;

import com.placementgo.backend.dashboard.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    
    @NotNull(message = "Status is required")
    private ApplicationStatus status;
}