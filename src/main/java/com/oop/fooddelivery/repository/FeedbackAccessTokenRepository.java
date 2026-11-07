package com.oop.fooddelivery.repository;

import com.oop.fooddelivery.model.FeedbackAccessToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackAccessTokenRepository
        extends JpaRepository<FeedbackAccessToken, Long> {

    // Fetch the linked Order while the repository query is active.
    @EntityGraph(attributePaths = "order")
    Optional<FeedbackAccessToken> findByTokenHash(String tokenHash);

    boolean existsByOrder_OrderId(Long orderId);
}
