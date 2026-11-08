package com.oop.fooddelivery.payment;

import com.oop.fooddelivery.model.PaymentMethod;
import com.oop.fooddelivery.model.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class CashPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentMethod supports() {
        return PaymentMethod.CASH;
    }

    @Override
    public PaymentStatus initialStatus() {
        // Cash is pending until an administrator confirms collection.
        return PaymentStatus.PENDING;
    }
}
