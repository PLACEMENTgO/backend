package com.placementgo.backend.payment.controller;

import com.placementgo.backend.payment.dto.CreateOrderResponse;
import com.placementgo.backend.payment.dto.SubscriptionStatusResponse;
import com.placementgo.backend.payment.dto.VerifyPaymentRequest;
import com.placementgo.backend.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /** Create a new Razorpay order for PRO subscription */
    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @AuthenticationPrincipal UUID userId) {
        try {
            log.info("Creating payment order for user: {}", userId);
            CreateOrderResponse response = paymentService.createOrder(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to create payment order for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /** Verify payment signature and activate subscription */
    @PostMapping("/verify")
    public ResponseEntity<SubscriptionStatusResponse> verify(
            @AuthenticationPrincipal UUID userId,
            @RequestBody VerifyPaymentRequest request) {
        try {
            log.info("Verifying payment for user: {}", userId);
            SubscriptionStatusResponse response = paymentService.verifyAndActivate(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid payment verification request for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to verify payment for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /** Get current subscription status */
    @GetMapping("/status")
    public ResponseEntity<SubscriptionStatusResponse> status(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(paymentService.getStatus(userId));
    }
}
