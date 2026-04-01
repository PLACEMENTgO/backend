package com.placementgo.backend.auth.util;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(
                    "THIS_IS_A_SUPER_LONG_256_BIT_SECRET_KEY_CHANGE_LATER".getBytes()
            );

    private static final long EXPIRY_SECONDS = 60 * 60 * 24;

    public String generateToken(UUID userId, String email) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plusSeconds(EXPIRY_SECONDS)))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(
                Jwts.parserBuilder()
                        .setSigningKey(KEY)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject()
        );
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Password reset tokens (15 min expiry) ────────────────────────────────

    private static final long RESET_EXPIRY_SECONDS = 15 * 60;

    public String generatePasswordResetToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("type", "password-reset")
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plusSeconds(RESET_EXPIRY_SECONDS)))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public String validatePasswordResetToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            if (!"password-reset".equals(claims.get("type"))) {
                throw new RuntimeException("Invalid token type");
            }
            return claims.getSubject(); // email
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired reset token");
        }
    }
}
