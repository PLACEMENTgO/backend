package com.placementgo.backend.referral.controller;

import com.placementgo.backend.referral.dto.CreateReferralRequest;
import com.placementgo.backend.referral.service.ReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/referrals")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReferralController {

    private final ReferralService referralService;

    @PostMapping
    public Object create(Authentication authentication,
                         @RequestBody CreateReferralRequest req) {
        UUID userId = extractUserIdFromAuth(authentication);
        return referralService.createReferral(userId, req);
    }

    @GetMapping
    public Object list(Authentication authentication) {
        UUID userId = extractUserIdFromAuth(authentication);
        return referralService.getAll(userId);
    }

    @GetMapping("/{id}")
    public Object get(@PathVariable UUID id) {
        return referralService.getById(id);
    }
    
    private UUID extractUserIdFromAuth(Authentication authentication) {
        // Tumhare JWT token se userId nikalo
        // Example:
        String userId = authentication.getName(); // ya authentication.getPrincipal()
        return UUID.fromString(userId);
    }
}