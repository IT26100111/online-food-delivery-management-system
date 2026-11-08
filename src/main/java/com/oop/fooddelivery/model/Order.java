package com.oop.fooddelivery.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @NotBlank(message = "Restaurant name is required.")
    @Size(max = 100, message = "Restaurant name must not exceed 100 characters.")
    private String restaurantName;

    @NotNull(message = "Order date is required.")
    private LocalDate orderDate;

    // Derived from OrderItem subtotals, not editable from the browser.
    private Double totalAmount;

    @NotNull(message = "Order status is required.")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @NotBlank(message = "Delivery address is required.")
    @Size(min = 5, max = 240,
            message = "Delivery address must be between 5 and 240 characters.")
    private String deliveryAddress;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> orderItems = new ArrayList<>();

    public Order() { }

    // Existing constructor retained for compatibility with other modules.
    public Order(Customer customer, String restaurantName, LocalDate orderDate,
                 Double totalAmount, OrderStatus status, String deliveryAddress) {
        this.customer = customer;
        this.restaurantName = restaurantName;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.deliveryAddress = deliveryAddress;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public Double getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public List<OrderItem> getOrderItems() { return orderItems; }

    // Retained because older code may use the setter. Recalculate on replacement.
    public void setOrderItems(List<OrderItem> orderItems) {
        // Keep the same JPA-managed collection instance (important with orphanRemoval).
        List<OrderItem> replacement = orderItems == null
                ? List.of() : new ArrayList<>(orderItems);
        this.orderItems.clear();
        this.orderItems.addAll(replacement);
        calculateTotalAmount();
    }

    public void addOrderItem(OrderItem item) {
        if (item == null) throw new IllegalArgumentException("Order item is required.");
        orderItems.add(item);
        calculateTotalAmount();
    }

    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        calculateTotalAmount();
    }

    public void calculateTotalAmount() {
        double total = 0.0;
        for (OrderItem item : orderItems) {
            if (item != null && item.getSubtotal() != null) total += item.getSubtotal();
        }
        this.totalAmount = total;
    }

    // The domain object owns its state-transition rule.
    public boolean changeStatus(OrderStatus newStatus) {
        if (newStatus == null) return false;
        if (status == null) {
            status = newStatus;
            return true;
        }
        if (!status.canTransitionTo(newStatus)) return false;
        status = newStatus;
        return true;
    }
}
