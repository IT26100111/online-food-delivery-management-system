
package com.oop.fooddelivery.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "order_tracking_tokens")
public class OrderTrackingToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Each order receives one secure tracking token.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            unique = true
    )
    private Order order;

    // Store only the token hash, never the actual token.
    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String tokenHash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    // Required by JPA
    protected OrderTrackingToken() {
    }

    public OrderTrackingToken(
            Order order,
            String tokenHash,
            Instant expiresAt) {

        this.order = Objects.requireNonNull(order);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.createdAt = Instant.now();
        this.expiresAt = Objects.requireNonNull(expiresAt);
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return !Instant.now().isBefore(expiresAt);
    }
}
