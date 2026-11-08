
package com.oop.fooddelivery.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "feedback_access_tokens")
public class FeedbackAccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Each Order has at most one review access token.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            unique = true
    )
    private Order order;

    // Store a SHA-256 hash, never the raw token.
    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    protected FeedbackAccessToken() {
        // Required by JPA
    }

    public FeedbackAccessToken(
            Order order,
            String tokenHash,
            Instant expiresAt) {

        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException(
                    "A saved Order is required."
            );
        }

        if (tokenHash == null ||
                !tokenHash.matches("[a-f0-9]{64}")) {
            throw new IllegalArgumentException(
                    "A valid SHA-256 token hash is required."
            );
        }

        if (expiresAt == null) {
            throw new IllegalArgumentException(
                    "Token expiration is required."
            );
        }

        this.order = order;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }
}
