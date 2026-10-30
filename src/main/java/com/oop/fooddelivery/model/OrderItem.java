package com.oop.fooddelivery.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;
    @ManyToOne
    @JoinColumn(name = "food_item_id")
    private FoodItem foodItem;

    private Integer quantity;
    private Double unitPrice;
    private Double subtotal;

    public OrderItem(){

    }

    public OrderItem(FoodItem foodItem,
                     Integer quantity
                     ){

        this.foodItem = foodItem;
        this.quantity = quantity;

        if(foodItem != null){
            this.unitPrice = foodItem.getPrice();
        }
        calculateSubtotal();

    }
    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }


    public FoodItem getFoodItem() {
        return foodItem;
    }

    public void setFoodItem(FoodItem foodItem) {
        this.foodItem = foodItem;

        if (foodItem != null) {
            this.unitPrice = foodItem.getPrice();
        }

        calculateSubtotal();
    }


    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;

        calculateSubtotal();
    }


    public Double getUnitPrice() {
        return unitPrice;
    }


    public Double getSubtotal() {
        return subtotal;
    }

    public void calculateSubtotal(){
        if(quantity != null && unitPrice != null){
            subtotal = quantity * unitPrice;
        }else{
            subtotal = 0.0;
        }
    }


}
