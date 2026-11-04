package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.Restaurant;

import java.util.List;

public interface RestaurantOperations {

    Restaurant saveRestaurant(Restaurant restaurant);

    List<Restaurant> getAllRestaurants();

    Restaurant getRestaurantById(Long id);

    void deleteRestaurant(Long id);
}