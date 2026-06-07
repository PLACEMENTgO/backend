package com.placementgo.backend.resume.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;


@Slf4j
@Component("ResumeGroqClient")
public class GroqClient {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final GroqProperties props;
    private final GroqRateLimiter rateLimiter;
    private final GroqApiKeyPool keyPool;

    public GroqClient(GroqProperties props, GroqRateLimiter rateLimiter, GroqApiKeyPool keyPool) {
        this.props = props;
        this.rateLimiter = rateLimiter;
        this.keyPool = keyPool;
        // Build the client WITHOUT a default Authorization header — we attach it per-request
        // so we can rotate keys on the fly.
        this.webClient = WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String generateContent(String prompt) {

        log.info("📡 Sending request to Groq model: {}", props.getModel());
        log.info("🌍 Base URL: {}", props.getBaseUrl());

        final String requestBody;
        try {
            requestBody = """
        {
          "model": "%s",
          "messages": [
            { "role": "user", "content": %s }
          ],
          "temperature": %s,
          "max_tokens": 4000
        }
        """.formatted(props.getModel(), mapper.writeValueAsString(prompt), props.getTemperature());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize Groq request", e);
        }

        int estimatedTokens = rateLimiter.estimateTokens(requestBody);
        try {
            rateLimiter.acquireTokens(estimatedTokens);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for Groq rate limit", ie);
        }

        log.info("📤 Request body size: {} chars", requestBody.length());

        // Determine how many keys to try. If pool is empty, fall back to the single legacy key.
        int poolSize = keyPool.size();
        int attemptsAllowed = Math.max(poolSize, 1);
        RuntimeException lastError = null;

        for (int attempt = 1; attempt <= attemptsAllowed; attempt++) {
            String apiKey = poolSize > 0 ? keyPool.nextKey() : props.getApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                throw new RuntimeException("No Groq API key available");
            }
            log.info("🔑 Groq attempt {}/{} using key …{}", attempt, attemptsAllowed,
                     apiKey.length() > 6 ? apiKey.substring(apiKey.length() - 4) : "****");

            try {
                JsonNode response = webClient.post()
                        .uri("/chat/completions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                        .bodyValue(requestBody)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, clientResponse -> {
                            HttpStatusCode status = clientResponse.statusCode();
                            log.error("❌ Groq returned HTTP error: {}", status);
                            return clientResponse.bodyToMono(String.class).map(body ->
                                    new WebClientResponseException(
                                            status.value(),
                                            "Groq error: " + body,
                                            null, null, null));
                        })
                        .bodyToMono(JsonNode.class)
                        .block();

                if (response == null) {
                    throw new RuntimeException("Groq API call returned null");
                }

                log.info("📥 Groq response received");
                return response
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();

            } catch (WebClientResponseException ex) {
                int status = ex.getStatusCode().value();
                // 429 = rate-limited, 401/403 = bad/exhausted key — rotate to next key.
                if (status == 429 || status == 401 || status == 403) {
                    log.warn("🔄 Key failed with HTTP {}, rotating to next key.", status);
                    if (poolSize > 0) keyPool.markFailed(apiKey);
                    lastError = new RuntimeException("Groq API key exhausted/rate-limited (HTTP " + status + ")", ex);
                    continue;
                }
                log.error("❌ Groq non-retryable error HTTP {}", status, ex);
                throw new RuntimeException("Groq API call failed (HTTP " + status + ")", ex);
            } catch (Exception e) {
                log.error("❌ Groq API exception:", e);
                lastError = new RuntimeException("Groq API call failed", e);
            }
        }

        throw lastError != null ? lastError : new RuntimeException("Groq API call failed after all keys exhausted");
    }
}
