
package com.oop.fooddelivery.repository;

import com.oop.fooddelivery.model.OrderTrackingToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderTrackingTokenRepository
        extends JpaRepository<OrderTrackingToken, Long> {

    // Find a tracking record using its hashed token.
    Optional<OrderTrackingToken> findByTokenHash(
            String tokenHash
    );

    // Prevent multiple active token records for one order.
    boolean existsByOrder_OrderId(Long orderId);
}
