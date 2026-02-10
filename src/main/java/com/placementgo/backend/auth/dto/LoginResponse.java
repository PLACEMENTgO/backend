package com.placementgo.backend.auth.dto;

import java.util.UUID;

public class LoginResponse {
    public UUID userId;
    public String token;

    public LoginResponse(UUID userId, String token) {
        this.userId = userId;
        this.token = token;
    }
}
