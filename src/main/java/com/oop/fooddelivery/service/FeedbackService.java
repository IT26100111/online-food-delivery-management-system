package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.Feedback;
import com.oop.fooddelivery.model.Order;
import com.oop.fooddelivery.repository.FeedbackRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final OrderService orderService;

    // Constructor-based Dependency Injection
    public FeedbackService(
            FeedbackRepository feedbackRepository,
            OrderService orderService) {

        this.feedbackRepository = feedbackRepository;
        this.orderService = orderService;
    }

    // READ: Get all feedback
    @Transactional(readOnly = true)
    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }

    // READ: Get feedback by ID
    @Transactional(readOnly = true)
    public Feedback getFeedbackById(Long id) {
        return feedbackRepository.findById(id)
                .orElse(null);
    }

    // READ: Get feedback for a specific order
    @Transactional(readOnly = true)
    public List<Feedback> getFeedbackByOrderId(Long orderId) {
        return feedbackRepository.findByOrder_OrderId(orderId);
    }

    // CREATE: Add new feedback
    @Transactional
    public Feedback createFeedback(
            Long orderId,
            Integer rating,
            String reviewMessage) {

        if (orderId == null) {
            throw new IllegalArgumentException(
                    "Please select an order."
            );
        }

        Order order = orderService.getOrderById(orderId);

        if (order == null) {
            throw new IllegalArgumentException(
                    "The selected order does not exist."
            );
        }

        // The Feedback constructor enforces:
        // 1. Existing Order
        // 2. DELIVERED Order status
        // 3. Rating between 1 and 5
        // 4. Non-empty review message
        Feedback feedback = new Feedback(
                order,
                rating,
                reviewMessage
        );

        return feedbackRepository.save(feedback);
    }

    // UPDATE: Change rating and review text
    @Transactional
    public Feedback updateFeedback(
            Long feedbackId,
            Integer rating,
            String reviewMessage) {

        Feedback existing = feedbackRepository
                .findById(feedbackId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Feedback not found."
                        )
                );

        // Existing historical records are preserved.
        // We do not re-check the Order status here.
        existing.setRating(rating);
        existing.setReviewMessage(reviewMessage);

        return feedbackRepository.save(existing);
    }

    // UPDATE: Change visibility status
    @Transactional
    public Feedback updateFeedbackStatus(
            Long feedbackId,
            String newStatus) {

        Feedback existing = feedbackRepository
                .findById(feedbackId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Feedback not found."
                        )
                );

        existing.changeStatus(newStatus);

        return feedbackRepository.save(existing);
    }

    // DELETE: Remove feedback only
    @Transactional
    public void deleteFeedback(Long feedbackId) {

        if (!feedbackRepository.existsById(feedbackId)) {
            throw new IllegalArgumentException(
                    "Feedback not found."
            );
        }

        feedbackRepository.deleteById(feedbackId);
    }
}