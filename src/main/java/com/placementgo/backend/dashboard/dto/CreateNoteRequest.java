package com.placementgo.backend.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateNoteRequest {
    
    @NotBlank(message = "Note cannot be empty")
    private String note;
}