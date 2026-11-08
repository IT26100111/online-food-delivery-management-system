package com.oop.fooddelivery.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class OrderValidationTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setup() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() { factory.close(); }

    @Test
    void acceptsCompleteAdminOrderForm() {
        Order order = new Order();
        order.setRestaurantName("Viva Kitchen");
        order.setOrderDate(LocalDate.of(2026, 11, 8));
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress("Colombo 05, Sri Lanka");
        assertTrue(validator.validate(order).isEmpty());
    }

    @Test
    void rejectsBlankRestaurantAndShortAddress() {
        Order order = new Order();
        order.setRestaurantName("  ");
        order.setOrderDate(LocalDate.of(2026, 11, 8));
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress("Hi");
        assertFalse(validator.validate(order).isEmpty());
    }

    @Test
    void rejectsMissingStatusAndDate() {
        Order order = new Order();
        order.setRestaurantName("Viva Kitchen");
        order.setDeliveryAddress("Colombo 05, Sri Lanka");
        assertTrue(validator.validate(order).size() >= 2);
    }
}
