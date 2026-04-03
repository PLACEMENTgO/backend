package com.placementgo.backend.resume.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;


@Slf4j
@Component("ResumeGroqClient")
public class GroqClient {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final GroqProperties props;
    private final GroqRateLimiter rateLimiter;

    public GroqClient(GroqProperties props, GroqRateLimiter rateLimiter) {
        this.props = props;
        this.rateLimiter = rateLimiter;
        this.webClient = WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String generateContent(String prompt) {

        log.info("📡 Sending request to Groq model: {}", props.getModel());
        log.info("🌍 Base URL: {}", props.getBaseUrl());

        try {

            String requestBody = """
        {
          "model": "%s",
          "messages": [
            { "role": "user", "content": %s }
          ],
          "temperature": %s,
          "max_tokens": 4000
        }
        """.formatted(props.getModel(), mapper.writeValueAsString(prompt), props.getTemperature());

            int estimatedTokens = rateLimiter.estimateTokens(requestBody);
            rateLimiter.acquireTokens(estimatedTokens);

            log.info("📤 Request body size: {} chars", requestBody.length());

            JsonNode response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            clientResponse -> {
                                log.error("❌ Groq returned HTTP error: {}", clientResponse.statusCode());
                                return clientResponse.bodyToMono(String.class)
                                        .map(body -> new RuntimeException("Groq API call failed: " + body));
                            }
                    )
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Groq API call failed");
            }

            log.info("📥 Groq response received");

            return response
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

        } catch (Exception e) {

            log.error("❌ Groq API exception:", e);

            throw new RuntimeException("Groq API call failed", e);
        }
    }

}
