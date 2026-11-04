package com.oop.fooddelivery.model;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restaurantId;

    @Column(nullable = false)
    private String restaurantName;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String cuisineType;

    @Column(nullable = false)
    private String status;

    public Restaurant() {
    }

    public Restaurant(String restaurantName,
                      String address,
                      String phoneNumber,
                      String cuisineType,
                      String status) {

        this.restaurantName = restaurantName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.cuisineType = cuisineType;
        this.status = status;
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

        if (status == null || status.isBlank()) {
            this.status = "Active";
        } else {
            this.status = status;
        }
    }

    public boolean isActive() {
        return "Active".equalsIgnoreCase(status);
    }
}