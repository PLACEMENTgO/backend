package com.placementgo.backend.payment.service;

import com.placementgo.backend.payment.dto.CreateOrderResponse;
import com.placementgo.backend.payment.dto.SubscriptionStatusResponse;
import com.placementgo.backend.payment.dto.VerifyPaymentRequest;
import com.placementgo.backend.payment.entity.Subscription;
import com.placementgo.backend.payment.enums.SubscriptionTier;
import com.placementgo.backend.payment.repository.SubscriptionRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {

    private static final int VALIDITY_DAYS = 30;

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    private final SubscriptionRepository subscriptionRepository;

    public PaymentService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /** Create a Razorpay order and persist a PENDING subscription record */
    public CreateOrderResponse createOrder(UUID userId, String planName) throws RazorpayException {
        SubscriptionTier tier = SubscriptionTier.fromString(planName);
        
        // Free tier cannot create orders
        if (tier.isFreeTier()) {
            throw new IllegalArgumentException("Cannot create order for free tier");
        }
        
        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", tier.getPriceInCents());
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "sub_" + userId.toString().substring(0, 8));
        orderRequest.put("payment_capture", true);

        Order order = client.orders.create(orderRequest);
        String orderId = order.get("id");
        log.info("Razorpay order created: {} for user {} - plan {}", orderId, userId, tier.name());

        // Save pending subscription
        Subscription sub = Subscription.builder()
                .userId(userId)
                .razorpayOrderId(orderId)
                .plan(tier.name())
                .status("PENDING")
                .amountPaise(tier.getPriceInCents())
                .build();
        subscriptionRepository.save(sub);

        String description = switch (tier) {
            case PRO -> "30-day Pro Access — 15 Resumes, 20 Job Searches, 50 Applications";
            case ENTERPRISE -> "30-day Enterprise Access — 50 Resumes, 60 Job Searches, 200 Applications";
            default -> "Premium Access";
        };

        return CreateOrderResponse.builder()
                .orderId(orderId)
                .amountPaise(tier.getPriceInCents())
                .currency("INR")
                .keyId(keyId)
                .planName("PlacementGO " + tier.getDisplayName())
                .description(description)
                .build();
    }

    /** Verify Razorpay signature and activate subscription */
    public SubscriptionStatusResponse verifyAndActivate(UUID userId, VerifyPaymentRequest req) {
        String expectedSignature = hmacSha256(req.getRazorpayOrderId() + "|" + req.getRazorpayPaymentId(), keySecret);

        if (!expectedSignature.equals(req.getRazorpaySignature())) {
            log.warn("Invalid Razorpay signature for order {}", req.getRazorpayOrderId());
            throw new IllegalArgumentException("Payment verification failed: invalid signature");
        }

        Optional<Subscription> subOpt = subscriptionRepository.findByRazorpayOrderId(req.getRazorpayOrderId());
        if (subOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + req.getRazorpayOrderId());
        }

        Subscription sub = subOpt.get();
        Instant now = Instant.now();
        sub.setRazorpayPaymentId(req.getRazorpayPaymentId());
        sub.setStatus("ACTIVE");
        sub.setActivatedAt(now);
        sub.setExpiresAt(now.plus(VALIDITY_DAYS, ChronoUnit.DAYS));
        subscriptionRepository.save(sub);

        log.info("Subscription ACTIVE for user {} — plan {} — payment {}", userId, sub.getPlan(), req.getRazorpayPaymentId());

        SubscriptionTier tier = SubscriptionTier.fromString(sub.getPlan());

        return SubscriptionStatusResponse.builder()
                .isPremium(!tier.isFreeTier())
                .plan(tier.name())
                .status("ACTIVE")
                .activatedAt(sub.getActivatedAt())
                .expiresAt(sub.getExpiresAt())
                .build();
    }

    /** Get current subscription status for a user */
    public SubscriptionStatusResponse getStatus(UUID userId) {
        Optional<Subscription> subOpt = subscriptionRepository.findTopByUserIdOrderByCreatedAtDesc(userId);

        if (subOpt.isEmpty()) {
            return SubscriptionStatusResponse.builder()
                    .isPremium(false)
                    .plan("STARTER")
                    .status("NONE")
                    .build();
        }

        Subscription sub = subOpt.get();
        boolean isActive = "ACTIVE".equals(sub.getStatus())
                && (sub.getExpiresAt() == null || sub.getExpiresAt().isAfter(Instant.now()));

        SubscriptionTier tier = isActive ? SubscriptionTier.fromString(sub.getPlan()) : SubscriptionTier.STARTER;

        return SubscriptionStatusResponse.builder()
                .isPremium(!tier.isFreeTier())
                .plan(tier.name())
                .status(sub.getStatus())
                .activatedAt(sub.getActivatedAt())
                .expiresAt(sub.getExpiresAt())
                .build();
    }

    private String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 failed", e);
        }
    }
}
