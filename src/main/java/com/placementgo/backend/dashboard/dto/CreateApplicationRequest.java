package com.placementgo.backend.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateApplicationRequest {
    
    @NotBlank(message = "Company name is required")
    private String company;
    
    @NotBlank(message = "Role is required")
    private String role;
    
    private String jobLink;
    
    private LocalDate appliedDate;
}