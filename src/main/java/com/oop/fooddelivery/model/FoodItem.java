
package com.oop.fooddelivery.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "food_items")
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foodItemId;

    @NotBlank(message = "Food name is required.")
    @Size(max = 100, message = "Food name must be at most 100 characters.")
    private String foodName;

    @Size(max = 255, message = "Restaurant name is too long.")
    private String restaurantName;

    // NEW: Real relationship with Restaurant.
    // Multiple FoodItems can belong to one Restaurant.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @NotBlank(message = "Category is required.")
    @Size(max = 100, message = "Category is too long.")
    private String category;

    @NotNull(message = "Price is required.")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero.")
    @DecimalMax(value = "9999999.99", message = "Price is too high.")
    private Double price;

    @NotBlank(message = "Availability is required.")
    @Pattern(
            regexp = "Available|Low Stock|Unavailable",
            message = "Select a valid availability status."
    )
    private String availability;

    @Size(max = 255, message = "Image URL must be at most 255 characters.")
    private String imageUrl;

    @NotBlank(message = "Description is required.")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters.")
    @Column(length = 1000)
    private String description;

    public FoodItem() {
    }

    public FoodItem(
            String foodName,
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

    // Legacy field accessors
    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    // NEW: Restaurant association accessors
    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;

        // Keep the legacy text column synchronized
        // while older pages still depend on it.
        if (restaurant != null) {
            this.restaurantName = restaurant.getRestaurantName();
        }
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
