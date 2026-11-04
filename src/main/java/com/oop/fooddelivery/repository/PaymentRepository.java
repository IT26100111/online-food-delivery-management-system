package com.oop.fooddelivery.repository;

import com.oop.fooddelivery.model.Payment;
import com.oop.fooddelivery.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByOrder_OrderIdAndPaymentStatus(Long orderId, PaymentStatus status);
}
