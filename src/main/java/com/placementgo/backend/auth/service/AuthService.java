package com.placementgo.backend.auth.service;

import com.placementgo.backend.auth.dto.LoginRequest;
import com.placementgo.backend.auth.dto.LoginResponse;
import com.placementgo.backend.auth.dto.RegisterRequest;
import com.placementgo.backend.auth.model.User;
import com.placementgo.backend.auth.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthService(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
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
}
