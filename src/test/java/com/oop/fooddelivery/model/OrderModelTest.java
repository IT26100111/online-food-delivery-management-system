package com.oop.fooddelivery.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class OrderModelTest {

    @Test
    void calculatesTotalFromOrderItemSubtotals() {
        FoodItem food = new FoodItem();
        food.setPrice(550.0);
        Order order = new Order();
        order.addOrderItem(new OrderItem(food, 2));
        order.addOrderItem(new OrderItem(food, 3));
        assertEquals(2750.0, order.getTotalAmount());
    }

    @Test
    void storesUnitPriceSnapshotWhenMenuPriceChanges() {
        FoodItem food = new FoodItem();
        food.setPrice(1000.0);
        OrderItem item = new OrderItem(food, 2);
        food.setPrice(1500.0);
        assertEquals(1000.0, item.getUnitPrice());
        assertEquals(2000.0, item.getSubtotal());
    }

    @Test
    void cannotSkipOrderStatusOrReverseFromDelivered() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        assertFalse(order.changeStatus(OrderStatus.READY));
        assertTrue(order.changeStatus(OrderStatus.PREPARING));
        assertTrue(order.changeStatus(OrderStatus.READY));
        assertTrue(order.changeStatus(OrderStatus.OUT_FOR_DELIVERY));
        assertTrue(order.changeStatus(OrderStatus.DELIVERED));
        assertFalse(order.changeStatus(OrderStatus.PENDING));
        assertFalse(order.changeStatus(null));
    }

    @Test
    void removingOrderItemRecalculatesTotal() {
        FoodItem food = new FoodItem();
        food.setPrice(400.0);
        Order order = new Order();
        OrderItem first = new OrderItem(food, 1);
        OrderItem second = new OrderItem(food, 2);
        order.addOrderItem(first);
        order.addOrderItem(second);
        order.removeOrderItem(first);
        assertEquals(800.0, order.getTotalAmount());
    }
}
