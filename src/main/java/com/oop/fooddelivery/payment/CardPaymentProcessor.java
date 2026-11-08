package com.oop.fooddelivery.payment;

import com.oop.fooddelivery.model.PaymentMethod;
import com.oop.fooddelivery.model.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class CardPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentMethod supports() {
        return PaymentMethod.CARD;
    }

    @Override
    public PaymentStatus initialStatus() {
        // Academic simulation only: NO real card is charged.
        // Admin must verify/mark as PAID manually.
        return PaymentStatus.PENDING;
    }
}
