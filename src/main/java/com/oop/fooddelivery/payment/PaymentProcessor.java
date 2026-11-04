package com.oop.fooddelivery.payment;

import com.oop.fooddelivery.model.PaymentMethod;
import com.oop.fooddelivery.model.PaymentStatus;

public interface PaymentProcessor {
    PaymentMethod supports();
    PaymentStatus initialStatus();
}
