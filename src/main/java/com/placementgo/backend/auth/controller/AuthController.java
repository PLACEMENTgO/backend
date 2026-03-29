package com.placementgo.backend.auth.controller;

import com.placementgo.backend.auth.dto.GoogleAuthRequest;
import com.placementgo.backend.auth.dto.LoginRequest;
import com.placementgo.backend.auth.dto.LoginResponse;
import com.placementgo.backend.auth.dto.RegisterRequest;
import com.placementgo.backend.auth.service.AuthService;
import com.placementgo.backend.auth.service.GoogleAuthService;
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
}
