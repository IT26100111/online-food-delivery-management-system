
package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.FeedbackAccessToken;
import com.oop.fooddelivery.model.Order;
import com.oop.fooddelivery.repository.FeedbackAccessTokenRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class FeedbackAccessTokenService {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private static final Duration TOKEN_VALIDITY =
            Duration.ofDays(90);

    private final FeedbackAccessTokenRepository tokenRepository;

    public FeedbackAccessTokenService(
            FeedbackAccessTokenRepository tokenRepository) {

        this.tokenRepository = tokenRepository;
    }

    // =========================================
    // GENERATE REVIEW ACCESS TOKEN
    // =========================================
    @Transactional
    public String generateTokenForOrder(Order order) {

        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException(
                    "A saved Order is required."
            );
        }

        Long orderId = order.getOrderId();

        // One token record per Order.
        if (tokenRepository.existsByOrder_OrderId(orderId)) {
            throw new IllegalStateException(
                    "A review token already exists for this Order."
            );
        }

        // Generate 32 cryptographically secure random bytes.
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);

        // Encode as a URL-safe token without padding.
        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        // Never store the raw token.
        String tokenHash = hashToken(rawToken);

        Instant expiresAt =
                Instant.now().plus(TOKEN_VALIDITY);

        FeedbackAccessToken accessToken =
                new FeedbackAccessToken(
                        order,
                        tokenHash,
                        expiresAt
                );

        tokenRepository.save(accessToken);

        // Return the raw token only to the trusted caller.
        return rawToken;
    }

    // =========================================
    // VALIDATE TOKEN AND FIND ASSOCIATED ORDER
    // =========================================
    @Transactional(readOnly = true)
    public Optional<Order> findOrderByToken(String rawToken) {

        // 32 random bytes produce 43 Base64 URL-safe
        // characters without padding.
        if (rawToken == null ||
                !rawToken.matches("[A-Za-z0-9_-]{43}")) {

            return Optional.empty();
        }

        String tokenHash = hashToken(rawToken);

        return tokenRepository.findByTokenHash(tokenHash)

                // Reject expired tokens.
                .filter(token ->
                        !token.isExpired(Instant.now())
                )

                // Retrieve the linked Order.
                .map(FeedbackAccessToken::getOrder);
    }

    // =========================================
    // HASH TOKEN USING SHA-256
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
                    "SHA-256 is unavailable.",
                    ex
            );
        }
    }
}
