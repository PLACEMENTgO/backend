package com.placementgo.backend.auth.controller;

import com.placementgo.backend.auth.dto.ForgotPasswordRequest;
import com.placementgo.backend.auth.dto.GoogleAuthRequest;
import com.placementgo.backend.auth.dto.LoginRequest;
import com.placementgo.backend.auth.dto.LoginResponse;
import com.placementgo.backend.auth.dto.RegisterRequest;
import com.placementgo.backend.auth.dto.ResetPasswordRequest;
import com.placementgo.backend.auth.service.AuthService;
import com.placementgo.backend.auth.service.GoogleAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    public AuthController(AuthService authService, GoogleAuthService googleAuthService) {
        this.authService = authService;
        this.googleAuthService = googleAuthService;
    }

    @PostMapping("/register")
    public LoginResponse register(@RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/google")
    public LoginResponse googleAuth(@RequestBody GoogleAuthRequest req) {
        return googleAuthService.authenticateGoogleUser(req.credential);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        try {
            authService.forgotPassword(req.email);
        } catch (Exception ignored) {
            // Never reveal success/failure to prevent enumeration
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest req) {
        try {
            authService.resetPassword(req.token, req.newPassword);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
