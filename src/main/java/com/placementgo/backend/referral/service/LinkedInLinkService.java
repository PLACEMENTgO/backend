package com.placementgo.backend.referral.service;

import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class LinkedInLinkService {

    public String generateSearchLink(String company, String role) {

        String query = company + " " + role;

        return "https://www.linkedin.com/search/results/people/?keywords="
                + query.replace(" ", "%20");
    }

}
