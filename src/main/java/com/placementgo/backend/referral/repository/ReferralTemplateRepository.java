package com.placementgo.backend.referral.repository;

import com.placementgo.backend.referral.entity.ReferralTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReferralTemplateRepository extends JpaRepository<ReferralTemplate, UUID> {

    List<ReferralTemplate> findByReferralId(UUID referralId);

}