package com.placementgo.backend.referral.service;

import com.placementgo.backend.referral.dto.ReferralTemplateResponse;
import com.placementgo.backend.referral.repository.ReferralTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.placementgo.backend.referral.entity.ReferralTemplate;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReferralTemplateService {

    private final ReferralTemplateRepository templateRepository;

    public List<ReferralTemplateResponse> getTemplates(UUID referralId) {

        List<ReferralTemplate> templates =
                templateRepository.findByReferralId(referralId);

        return templates.stream()
                .map(t -> ReferralTemplateResponse.builder()
                        .type(t.getType().name())
                        .message(t.getMessage())
                        .version(t.getVersion())
                        .build())
                .collect(Collectors.toList());
    }
}