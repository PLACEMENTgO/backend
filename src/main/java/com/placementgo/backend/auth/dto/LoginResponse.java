package com.placementgo.backend.auth.dto;

import java.util.UUID;

public class LoginResponse {
    public UUID userId;
    public String token;
    public String name;
    public String email;
    public String profilePicture;

    public LoginResponse(UUID userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public LoginResponse(UUID userId, String token, String name, String email, String profilePicture) {
        this.userId = userId;
        this.token = token;
        this.name = name;
        this.email = email;
        this.profilePicture = profilePicture;
    }
}
