package com.oop.fooddelivery.model;

import jakarta.persistence.*;

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
    private  Customer customer;

    private String restaurantName;

    private LocalDate orderDate;

    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String deliveryAddress;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "order_id")
    private List<OrderItem>orderItems = new ArrayList<>();

    public Order() {
    }


    // Constructor we can use when creating Order objects manually
    public Order(Customer customer,
                 String restaurantName,
                 LocalDate orderDate,
                 Double totalAmount,
                 OrderStatus status,
                 String deliveryAddress) {

        this.customer = customer;
        this.restaurantName = restaurantName;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.deliveryAddress = deliveryAddress;
    }


    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }


    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }


    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }


    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }


    public Double getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }


    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public List<OrderItem>getOrderItems(){
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems){
        this.orderItems = orderItems;
    }
    public void addOrderItem(OrderItem item){
        orderItems.add(item);
        calculateTotalAmount();
    }

    public void removeOrderItem(OrderItem item){
        orderItems.remove(item);
        calculateTotalAmount();
    }

    public void calculateTotalAmount(){
        double total = 0.0;

        for(OrderItem item : orderItems){
            if(item.getSubtotal() != null){
                total += item.getSubtotal();
            }
        }
        this.totalAmount = total;
    }
    public boolean changeStatus(OrderStatus newStatus){
        if(status == null){
            status = newStatus;
            return true;
        }
        if(status.canTransitionTo(newStatus)){
            status = newStatus;
            return true;
        }
        return false;
    }
}