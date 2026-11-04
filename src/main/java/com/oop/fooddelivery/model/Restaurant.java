
package com.oop.fooddelivery.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restaurantId;

    @NotBlank(message = "Restaurant name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Restaurant name must be between 2 and 100 characters"
    )
    @Column(nullable = false, length = 100)
    private String restaurantName;

    @NotBlank(message = "Address is required")
    @Size(
            max = 255,
            message = "Address cannot exceed 255 characters"
    )
    @Column(nullable = false)
    private String address;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone number must contain exactly 10 digits"
    )
    @Column(nullable = false, length = 10)
    private String phoneNumber;

    @NotBlank(message = "Cuisine type is required")
    @Size(
            max = 60,
            message = "Cuisine type cannot exceed 60 characters"
    )
    @Column(nullable = false, length = 60)
    private String cuisineType;

    @NotBlank(message = "Status is required")
    @Pattern(
            regexp = "^(Active|Inactive)$",
            message = "Status must be Active or Inactive"
    )
    @Column(nullable = false, length = 20)
    private String status;

    public Restaurant() {
    }

    public Restaurant(
            String restaurantName,
            String address,
            String phoneNumber,
            String cuisineType,
            String status) {

        this.restaurantName = restaurantName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.cuisineType = cuisineType;
        setStatus(status);
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return "Active".equals(status);
    }

    public boolean hasValidStatus() {
        return "Active".equals(status)
                || "Inactive".equals(status);
    }
}
