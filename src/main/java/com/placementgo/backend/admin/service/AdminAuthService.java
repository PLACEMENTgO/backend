package com.placementgo.backend.admin.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminAuthService {

    @Value("${admin.username:placementgo_admin}")
    private String adminUsername;

    @Value("${admin.password:PG@dm!n2026#Secure}")
    private String adminPassword;

    @Value("${admin.jwt.secret:PlacementGoAdminJwtSecretKey2026VerySecureAndLongEnoughForHS256Algorithm}")
    private String jwtSecret;

    @Value("${admin.jwt.expiration:28800000}") // 8 hours default
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validates admin credentials
     */
    public boolean validateCredentials(String username, String password) {
        return adminUsername.equals(username) && adminPassword.equals(password);
    }

    /**
     * Generates JWT token for admin
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        claims.put("username", username);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts username from token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Checks if token has admin role
     */
    public boolean isAdminToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return "ADMIN".equals(claims.get("role"));
        } catch (Exception e) {
            return false;
        }
    }

    public long getJwtExpiration() {
        return jwtExpiration;
    }
}
