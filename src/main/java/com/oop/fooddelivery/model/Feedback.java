package com.oop.fooddelivery.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    // Association: Feedback belongs to an existing Order.
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000, nullable = false)
    private String reviewMessage;

    @Column(nullable = false)
    private LocalDate feedbackDate;

    private String feedbackStatus;

    protected Feedback() {
        // Required by JPA
    }

    public Feedback(Order order,
                    Integer rating,
                    String reviewMessage) {

        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException(
                    "Select an existing order."
            );
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException(
                    "Feedback is allowed only for delivered orders."
            );
        }

        this.order = order;
        setRating(rating);
        setReviewMessage(reviewMessage);
        this.feedbackDate = LocalDate.now();
        this.feedbackStatus = "Visible";
    }

    public void setRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5."
            );
        }

        this.rating = rating;
    }

    public void setReviewMessage(String reviewMessage) {
        if (reviewMessage == null ||
                reviewMessage.isBlank() ||
                reviewMessage.length() > 1000) {

            throw new IllegalArgumentException(
                    "Review must contain 1 to 1000 characters."
            );
        }

        this.reviewMessage = reviewMessage.trim();
    }

    public void changeStatus(String newStatus) {
        if (!"Visible".equals(newStatus) &&
                !"Hidden".equals(newStatus)) {

            throw new IllegalArgumentException(
                    "Feedback status must be Visible or Hidden."
            );
        }

        this.feedbackStatus = newStatus;
    }

    public String getCustomerName() {
        if (order == null || order.getCustomer() == null) {
            return "Unknown Customer";
        }

        return order.getCustomer().getFullName();
    }

    public String displayDetails() {
        return getCustomerName() + " - " +
                rating + " stars - " +
                feedbackStatus;
    }

    public Long getFeedbackId() {
        return feedbackId;
    }

    public Order getOrder() {
        return order;
    }

    public Integer getRating() {
        return rating;
    }

    public String getReviewMessage() {
        return reviewMessage;
    }

    public LocalDate getFeedbackDate() {
        return feedbackDate;
    }

    public String getFeedbackStatus() {
        return feedbackStatus;
    }
}