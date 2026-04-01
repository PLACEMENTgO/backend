package com.placementgo.backend.auth.service;

import com.placementgo.backend.auth.dto.LoginRequest;
import com.placementgo.backend.auth.dto.LoginResponse;
import com.placementgo.backend.auth.dto.RegisterRequest;
import com.placementgo.backend.auth.model.User;
import com.placementgo.backend.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Value("${app.base-url:https://placementgo.in}")
    private String appBaseUrl;

    public AuthService(UserService userService, JwtUtil jwtUtil, EmailService emailService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public LoginResponse register(RegisterRequest req) {
        User user = userService.create(req.email, req.password);
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new LoginResponse(user.getId(), token);
    }

    public LoginResponse login(LoginRequest req) {
        User user = userService.validate(req.email, req.password);
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new LoginResponse(user.getId(), token);
    }

    public void forgotPassword(String email) {
        // Silently skip if email doesn't exist — prevents enumeration
        if (!userService.existsByEmail(email)) return;
        String token = jwtUtil.generatePasswordResetToken(email);
        String resetLink = appBaseUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(email, resetLink);
    }

    public void resetPassword(String token, String newPassword) {
        String email = jwtUtil.validatePasswordResetToken(token);
        userService.updatePassword(email, newPassword);
    }
}

