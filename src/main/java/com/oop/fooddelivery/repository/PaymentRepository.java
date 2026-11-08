package com.oop.fooddelivery.repository;

import com.oop.fooddelivery.model.Payment;
import com.oop.fooddelivery.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByOrder_OrderIdAndPaymentStatus(Long orderId, PaymentStatus status);

    // Used when deciding whether an order can be edited or deleted.
    boolean existsByOrder_OrderId(Long orderId);

    Optional<Payment> findTopByOrder_OrderIdOrderByPaymentIdDesc(Long orderId);
}
