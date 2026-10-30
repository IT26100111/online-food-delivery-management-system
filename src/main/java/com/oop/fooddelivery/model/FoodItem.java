package com.oop.fooddelivery.model;

import jakarta.persistence.*;

@Entity
@Table(name = "food_items")
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foodItemId;

    private String foodName;
    private String restaurantName;
    private String category;
    private Double price;
    private String availability;
    private String imageUrl;

    @Column(length = 1000)
    private String description;

    public FoodItem() {
    }

    public FoodItem(String foodName,
                    String restaurantName,
                    String category,
                    Double price,
                    String availability,
                    String imageUrl,
                    String description) {
        this.foodName = foodName;
        this.restaurantName = restaurantName;
        this.category = category;
        this.price = price;
        this.availability = availability;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public String displayDetails() {
        return foodName + " - " + category + " - Rs. " + price;
    }

    public Long getFoodItemId() {
        return foodItemId;
    }

    public void setFoodItemId(Long foodItemId) {
        this.foodItemId = foodItemId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
