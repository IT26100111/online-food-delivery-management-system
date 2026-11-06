
package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.FoodItem;
import com.oop.fooddelivery.repository.FoodItemRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FoodItemService {

    private final FoodItemRepository foodItemRepository;

    public FoodItemService(
            FoodItemRepository foodItemRepository) {
        this.foodItemRepository = foodItemRepository;
    }

    // CREATE / UPDATE
    public FoodItem saveFoodItem(FoodItem foodItem) {
        return foodItemRepository.save(foodItem);
    }

    // READ ALL - Admin
    public List<FoodItem> getAllFoodItems() {
        return foodItemRepository.findAll();
    }

    // READ ONE
    public FoodItem getFoodItemById(Long id) {
        return foodItemRepository.findById(id).orElse(null);
    }

    // READ BY RESTAURANT - Admin or internal use
    public List<FoodItem> getFoodItemsByRestaurant(
            Long restaurantId) {

        return foodItemRepository
                .findByRestaurant_RestaurantId(restaurantId);
    }

    // READ AVAILABLE FOOD BY RESTAURANT - Customer
    public List<FoodItem> getAvailableFoodItemsByRestaurant(
            Long restaurantId) {

        return foodItemRepository
                .findByRestaurant_RestaurantIdAndAvailability(
                        restaurantId,
                        "Available"
                );
    }

    // DELETE
    public void deleteFoodItem(Long id) {
        foodItemRepository.deleteById(id);
    }
}
