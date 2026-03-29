package com.placementgo.backend.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.placementgo.backend.auth.dto.LoginResponse;
import com.placementgo.backend.auth.model.User;
import com.placementgo.backend.auth.repository.UserRepository;
import com.placementgo.backend.auth.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Slf4j
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthService(
            UserRepository userRepository,
            JwtUtil jwtUtil,
            @Value("${google.oauth.client-id}") String clientId
    ) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public LoginResponse authenticateGoogleUser(String credential) {
        try {
            GoogleIdToken idToken = verifier.verify(credential);

            if (idToken == null) {
                throw new RuntimeException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            log.info("Google auth for: {} ({})", name, email);

            // Find or create user
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setName(name);
                        newUser.setProfilePicture(picture);
                        newUser.setAuthProvider("GOOGLE");
                        // No password for Google users
                        return userRepository.save(newUser);
                    });

            // Update profile info if user already exists (in case they changed their Google profile)
            if ("GOOGLE".equals(user.getAuthProvider())) {
                boolean updated = false;
                if (name != null && !name.equals(user.getName())) {
                    user.setName(name);
                    updated = true;
                }
                if (picture != null && !picture.equals(user.getProfilePicture())) {
                    user.setProfilePicture(picture);
                    updated = true;
                }
                if (updated) {
                    userRepository.save(user);
                }
            }

            String token = jwtUtil.generateToken(user.getId(), user.getEmail());
            return new LoginResponse(user.getId(), token, user.getName(), user.getEmail(), user.getProfilePicture());

        } catch (Exception e) {
            log.error("Google auth failed: {}", e.getMessage());
            throw new RuntimeException("Google authentication failed: " + e.getMessage());
        }
    }
}
