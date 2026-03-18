package com.placementgo.backend.jd_intel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {

    @Value("${jd-intel.search-api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<String> search(String query) {
        log.info("Searching for: {}", query);

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Search API Key is missing. Returning empty results.");
            return Collections.emptyList();
        }

        String url = UriComponentsBuilder.fromUriString("https://serpapi.com/search.json")
                .queryParam("engine", "google")
                .queryParam("q", query)
                .queryParam("location", "Austin, Texas, United States")
                .queryParam("google_domain", "google.com")
                .queryParam("hl", "en")
                .queryParam("gl", "us")
                .queryParam("api_key", apiKey)
                .toUriString();

        try {
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                return Collections.emptyList();
            }

            // SerpApi returns organic_results
            Object hitsObj = response.get("organic_results");
            List<?> hitsRaw = null;
            if (!(hitsObj instanceof List<?>)) {
                // fallback to old-style items key
                hitsObj = response.get("items");
                if (!(hitsObj instanceof List<?>)) {
                    return Collections.emptyList();
                }
            }

            return hitsRaw.stream()
                    .filter(hit -> hit instanceof Map<?, ?>)
                    .map(hit -> (Map<?, ?>) hit)
                    .map(itemMap -> itemMap.get("link"))
                    .filter(link -> link instanceof String)
                    .map(link -> (String) link)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error during search: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
