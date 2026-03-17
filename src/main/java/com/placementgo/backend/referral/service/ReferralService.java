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

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralRequestRepository referralRepository;
    private final ReferralTemplateRepository templateRepository;
    private final LinkedInLinkService linkedInLinkService;
    private final JobParserService jobParserService;

    private static final String BASE_SHARE_URL = "http://localhost:8080/r/";

    public CreateReferralResponse createReferral(UUID userId, CreateReferralRequest request) {

        JobParserService.JobDetails jobDetails = jobParserService.parseJobDescription(request.getJobDescription());
        String company = jobDetails.getCompany();
        String role = jobDetails.getRoleTitle();

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
                .company(company)
                .role(role)
                .build();
    }

    private List<ReferralTemplate> generateTemplates(UUID referralId, String company, String role) {
        List<ReferralTemplate> list = new ArrayList<>();

        list.add(ReferralTemplate.builder()
                .referralId(referralId)
                .type(TemplateType.SHORT)
                .message("Hi [Name],\n\nI saw you're at " + company + ". I'm applying for the " + role + " position and would love to connect!\n\nBest,\n[Your Name]")
                .version(1)
                .build());

        list.add(ReferralTemplate.builder()
                .referralId(referralId)
                .type(TemplateType.PROFESSIONAL)
                .message("Hello [Name],\n\nI noticed you work at " + company + " and wanted to reach out regarding the " + role + " position I'm applying for.\n\nI'd appreciate any insights you could share about the team and role.\n\nThank you,\n[Your Name]")
                .version(1)
                .build());

        list.add(ReferralTemplate.builder()
                .referralId(referralId)
                .type(TemplateType.CASUAL)
                .message("Hey [Name]!\n\nI'm applying for the " + role + " role at " + company + " and would love to chat about your experience there.\n\nWould you be open to a quick coffee chat?\n\nCheers,\n[Your Name]")
                .version(1)
                .build());

        return list;
    }

    public List<ReferralSummaryResponse> getAll(UUID userId) {
        List<ReferralRequest> referrals = referralRepository.findByUserId(userId);
        List<ReferralSummaryResponse> responses = new ArrayList<>();

        for (ReferralRequest r : referrals) {
            responses.add(ReferralSummaryResponse.builder()
                    .referralId(r.getId())
                    .shareLink(BASE_SHARE_URL + r.getToken())
                    .createdAt(r.getCreatedAt())
                    .build());
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
                .company(referral.getCompany())
                .role(referral.getRole())
                .build();
    }

    public Object getByToken(String token) {
        ReferralRequest referral = referralRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid referral token"));
        return referral.getLinkedinSearchLink();
    }
}