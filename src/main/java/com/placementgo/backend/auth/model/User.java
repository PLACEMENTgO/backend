package com.placementgo.backend.auth.model;

import jakarta.persistence.*;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Setter
    @Column(nullable = false, unique = true)
    private String email;

    @Setter
    @Column // nullable for Google OAuth users
    private String passwordHash;

    @Setter
    private String name;

    @Setter
    @Column(length = 500)
    private String profilePicture;

    @Setter
    @Column(nullable = false)
    private String authProvider = "LOCAL"; // LOCAL or GOOGLE

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // getters

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getName() {
        return name;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
