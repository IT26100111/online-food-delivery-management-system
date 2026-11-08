package com.oop.fooddelivery.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    // The Order is not owned by Payment: deleting Payment never deletes Order.
    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    // Snapshot of the order total when payment is created.
    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    protected Payment() {
    }

    public Payment(Order order, PaymentMethod paymentMethod, LocalDate paymentDate) {
        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException("Select an existing order.");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Select a payment method.");
        }
        if (order.getTotalAmount() == null || order.getTotalAmount() <= 0) {
            throw new IllegalArgumentException("Order total must be greater than zero.");
        }
        this.order = order;
        this.paymentMethod = paymentMethod;
        this.amount = order.getTotalAmount();
        this.paymentDate = paymentDate;
        this.paymentStatus = PaymentStatus.PENDING;
    }

    public boolean changeStatus(PaymentStatus newStatus) {
        if (!paymentStatus.canTransitionTo(newStatus)) return false;
        paymentStatus = newStatus;
        return true;
    }

    public String displayDetails() {
        return "Order #" + order.getOrderId() + " - " +
                paymentMethod.getDisplayName() + " - " + paymentStatus.getDisplayName();
    }

    public Long getPaymentId() { return paymentId; }
    public Order getOrder() { return order; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public Double getAmount() { return amount; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
}
