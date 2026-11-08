
package com.oop.fooddelivery.repository;

import com.oop.fooddelivery.model.Feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository
        extends JpaRepository<Feedback, Long> {

    // Find feedback associated with an Order.
    List<Feedback> findByOrder_OrderId(Long orderId);

    // Find feedback by rating.
    List<Feedback> findByRating(Integer rating);

    // Find feedback by visibility status.
    List<Feedback> findByFeedbackStatus(
            String feedbackStatus
    );

    // Check whether an Order already has feedback.
    boolean existsByOrder_OrderId(Long orderId);
}
