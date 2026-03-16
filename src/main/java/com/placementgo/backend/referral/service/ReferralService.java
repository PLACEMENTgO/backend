package com.placementgo.backend.referral.service;

import com.placementgo.backend.referral.dto.CreateReferralRequest;
import com.placementgo.backend.referral.dto.CreateReferralResponse;
import com.placementgo.backend.referral.dto.ReferralSummaryResponse;
import com.placementgo.backend.referral.entity.ReferralRequest;
import com.placementgo.backend.referral.entity.ReferralTemplate;
import com.placementgo.backend.referral.enums.TemplateType;
import com.placementgo.backend.referral.repository.ReferralRequestRepository;
import com.placementgo.backend.referral.repository.ReferralTemplateRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralRequestRepository referralRepository;
    private final ReferralTemplateRepository templateRepository;
    private final LinkedInLinkService linkedInLinkService;

    private static final String BASE_SHARE_URL = "http://localhost:8080/r/";

    public CreateReferralResponse createReferral(UUID userId, CreateReferralRequest request) {

        String company = "Company";
        String role = "Software Engineer";

        String linkedinLink = linkedInLinkService.generateSearchLink(company, role);

        String token = UUID.randomUUID().toString().substring(0, 8);

        ReferralRequest referral = ReferralRequest.builder()
                .userId(userId)
                .jobDescription(request.getJobDescription())
                .company(company)
                .role(role)
                .linkedinSearchLink(linkedinLink)
                .token(token)
                .createdAt(Instant.now())
                .build();

        referralRepository.save(referral);

        List<ReferralTemplate> templates = generateTemplates(referral.getId(), company, role);

        templateRepository.saveAll(templates);

        Map<String, String> templateMap = new HashMap<>();

        for (ReferralTemplate t : templates) {
            templateMap.put(t.getType().name(), t.getMessage());
        }

        return CreateReferralResponse.builder()
                .referralId(referral.getId())
                .shareLink(BASE_SHARE_URL + token)
                .linkedinSearchLink(linkedinLink)
                .templates(templateMap)
                .build();
    }

    private List<ReferralTemplate> generateTemplates(UUID referralId, String company, String role) {

        List<ReferralTemplate> list = new ArrayList<>();

        list.add(
                ReferralTemplate.builder()
                        .referralId(referralId)
                        .type(TemplateType.SHORT)
                        .message("Hi, I saw you're at " + company + ". Could you refer me for " + role + "?")
                        .version(1)
                        .build()
        );

        list.add(
                ReferralTemplate.builder()
                        .referralId(referralId)
                        .type(TemplateType.PROFESSIONAL)
                        .message("Hello, I noticed you work at " + company + ". I’m applying for the " + role + " role and would appreciate a referral.")
                        .version(1)
                        .build()
        );

        list.add(
                ReferralTemplate.builder()
                        .referralId(referralId)
                        .type(TemplateType.CASUAL)
                        .message("Hey! I'm applying for " + role + " at " + company + ". Would love a referral if possible.")
                        .version(1)
                        .build()
        );

        return list;
    }

    public List<ReferralSummaryResponse> getAll(UUID userId) {

        List<ReferralRequest> referrals = referralRepository.findByUserId(userId);

        List<ReferralSummaryResponse> responses = new ArrayList<>();

        for (ReferralRequest r : referrals) {

            responses.add(
                    ReferralSummaryResponse.builder()
                            .referralId(r.getId())
                            .shareLink(BASE_SHARE_URL + r.getToken())
                            .createdAt(r.getCreatedAt())
                            .build()
            );
        }

        return responses;
    }

    public CreateReferralResponse getById(UUID id) {

        ReferralRequest referral = referralRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Referral not found"));

        List<ReferralTemplate> templates = templateRepository.findByReferralId(id);

        Map<String, String> templateMap = new HashMap<>();

        for (ReferralTemplate t : templates) {
            templateMap.put(t.getType().name(), t.getMessage());
        }

        return CreateReferralResponse.builder()
                .referralId(referral.getId())
                .shareLink(BASE_SHARE_URL + referral.getToken())
                .linkedinSearchLink(referral.getLinkedinSearchLink())
                .templates(templateMap)
                .build();
    }

    public Object getByToken(String token) {

        ReferralRequest referral = referralRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid referral token"));

        return referral.getLinkedinSearchLink();
    }
}