
package com.oop.fooddelivery.repository;

import com.oop.fooddelivery.model.FoodItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodItemRepository
        extends JpaRepository<FoodItem, Long> {

    // Retrieve food items belonging to one restaurant.
    List<FoodItem> findByRestaurant_RestaurantId(
            Long restaurantId
    );

    // Retrieve only available food items for customers.
    List<FoodItem> findByRestaurant_RestaurantIdAndAvailability(
            Long restaurantId,
            String availability
    );

}
