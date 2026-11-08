
package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.Order;
import com.oop.fooddelivery.model.OrderTrackingToken;
import com.oop.fooddelivery.repository.OrderTrackingTokenRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class OrderTrackingTokenService {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final OrderTrackingTokenRepository tokenRepository;

    public OrderTrackingTokenService(
            OrderTrackingTokenRepository tokenRepository) {

        this.tokenRepository = tokenRepository;
    }

    // =========================================
    // GENERATE SECURE TRACKING TOKEN
    // =========================================
    @Transactional
    public String createTrackingToken(Order order) {

        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException(
                    "A saved order is required."
            );
        }

        // Each order should have only one tracking token.
        if (tokenRepository.existsByOrder_OrderId(
                order.getOrderId())) {

            throw new IllegalStateException(
                    "Tracking token already exists for this order."
            );
        }

        // Generate 32 cryptographically secure random bytes.
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);

        // URL-safe token, suitable for private tracking links.
        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        // Store only its SHA-256 hash in MySQL.
        String tokenHash = hashToken(rawToken);

        // Tracking access expires after 90 days.
        Instant expiresAt = Instant.now()
                .plus(90, ChronoUnit.DAYS);

        OrderTrackingToken trackingToken =
                new OrderTrackingToken(
                        order,
                        tokenHash,
                        expiresAt
                );

        tokenRepository.save(trackingToken);

        // Return the raw token only to the caller so it
        // can be included in the customer's private link.
        return rawToken;
    }

    // =========================================
    // VALIDATE TRACKING TOKEN
    // =========================================
    @Transactional(readOnly = true)
    public Optional<Order> findOrderByToken(String rawToken) {

        if (rawToken == null ||
                !rawToken.matches("[A-Za-z0-9_-]{43}")) {

            return Optional.empty();
        }

        String tokenHash = hashToken(rawToken);

        return tokenRepository
                .findByTokenHash(tokenHash)
                .filter(token -> !token.isExpired())
                .map(OrderTrackingToken::getOrder);
    }

    // =========================================
    // SHA-256 HASHING
    // =========================================
    private String hashToken(String rawToken) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 hashing is unavailable.",
                    ex
            );
        }
    }
}
