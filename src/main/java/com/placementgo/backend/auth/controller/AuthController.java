package com.placementgo.backend.auth.controller;

import com.placementgo.backend.auth.dto.LoginRequest;
import com.placementgo.backend.auth.dto.LoginResponse;
import com.placementgo.backend.auth.dto.RegisterRequest;
import com.placementgo.backend.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public LoginResponse register(@RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }
}
