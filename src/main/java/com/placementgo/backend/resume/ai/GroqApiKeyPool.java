package com.placementgo.backend.resume.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Manages a pool of Groq API keys with round-robin rotation and per-key cooldown
 * when a key is rate-limited (HTTP 429) or has invalid auth (401/403).
 *
 * Configure either:
 *   GROQ_API_KEYS=key1,key2,key3        (preferred — comma separated)
 *   GROQ_API_KEY=singlekey               (legacy single key)
 *
 * The property {@code resume.groq.api-key} (single key) is kept for backwards
 * compatibility — if {@code resume.groq.api-keys} is empty we fall back to it.
 */
@Component
@Slf4j
public class GroqApiKeyPool {

    @Value("${resume.groq.api-keys:}")
    private String apiKeysCsv;

    @Value("${resume.groq.api-key:}")
    private String legacyApiKey;

    /** How long a key stays "cooling down" after being rate-limited. */
    private static final long COOLDOWN_MS = 60_000L;

    private final List<String> keys = new ArrayList<>();
    private final List<Long> cooldownUntil = new ArrayList<>();
    private final AtomicInteger cursor = new AtomicInteger(0);

    @PostConstruct
    void init() {
        List<String> parsed = new ArrayList<>();
        if (apiKeysCsv != null && !apiKeysCsv.isBlank()) {
            parsed.addAll(Arrays.stream(apiKeysCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList()));
        }
        if (legacyApiKey != null && !legacyApiKey.isBlank() && !parsed.contains(legacyApiKey.trim())) {
            parsed.add(legacyApiKey.trim());
        }
        if (parsed.isEmpty()) {
            log.warn("⚠️ No Groq API keys configured (resume.groq.api-keys / resume.groq.api-key both empty).");
        } else {
            log.info("🔑 Groq API key pool initialized with {} key(s).", parsed.size());
        }
        synchronized (keys) {
            keys.addAll(parsed);
            for (int i = 0; i < parsed.size(); i++) cooldownUntil.add(0L);
        }
    }

    public int size() {
        synchronized (keys) {
            return keys.size();
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Get the next currently-available key (round-robin, skipping keys in cooldown).
     * If every key is cooling down, returns the one whose cooldown expires soonest.
     */
    public String nextKey() {
        synchronized (keys) {
            if (keys.isEmpty()) return null;
            long now = System.currentTimeMillis();
            int n = keys.size();
            int startIdx = Math.floorMod(cursor.getAndIncrement(), n);
            int bestIdx = startIdx;
            long bestCooldown = cooldownUntil.get(startIdx);
            for (int i = 0; i < n; i++) {
                int idx = Math.floorMod(startIdx + i, n);
                long cd = cooldownUntil.get(idx);
                if (cd <= now) return keys.get(idx);
                if (cd < bestCooldown) { bestCooldown = cd; bestIdx = idx; }
            }
            // All keys cooling down — return the one closest to availability anyway.
            log.warn("⚠️ All Groq keys are cooling down; falling back to soonest-available key.");
            return keys.get(bestIdx);
        }
    }

    /** Mark the given key as rate-limited / failing for the cooldown window. */
    public void markFailed(String key) {
        if (key == null) return;
        synchronized (keys) {
            int idx = keys.indexOf(key);
            if (idx >= 0) {
                cooldownUntil.set(idx, System.currentTimeMillis() + COOLDOWN_MS);
                log.warn("🚫 Groq key #{} marked unavailable for {}s.", idx, COOLDOWN_MS / 1000);
            }
        }
    }

    /** Number of keys currently NOT in cooldown. */
    public int availableCount() {
        synchronized (keys) {
            long now = System.currentTimeMillis();
            int count = 0;
            for (long t : cooldownUntil) if (t <= now) count++;
            return count;
        }
    }
}
