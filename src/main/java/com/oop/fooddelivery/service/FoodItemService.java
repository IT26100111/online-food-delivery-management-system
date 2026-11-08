package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.FoodItem;
import com.oop.fooddelivery.repository.FoodItemRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FoodItemService {

    private final FoodItemRepository foodItemRepository;

    public FoodItemService(FoodItemRepository foodItemRepository) {
        this.foodItemRepository = foodItemRepository;
    }

    // CREATE / UPDATE - the controller validates form input and restaurant.
    @Transactional
    public FoodItem saveFoodItem(FoodItem foodItem) {
        if (foodItem == null) {
            throw new IllegalArgumentException("Food item details are required.");
        }
        return foodItemRepository.save(foodItem);
    }

    // READ ALL - used by administrators.
    @Transactional(readOnly = true)
    public List<FoodItem> getAllFoodItems() {
        return foodItemRepository.findAll();
    }

    // READ ONE.
    @Transactional(readOnly = true)
    public FoodItem getFoodItemById(Long id) {
        if (id == null) return null;
        return foodItemRepository.findById(id).orElse(null);
    }

    // READ BY RESTAURANT.
    @Transactional(readOnly = true)
    public List<FoodItem> getFoodItemsByRestaurant(Long restaurantId) {
        return foodItemRepository.findByRestaurant_RestaurantId(restaurantId);
    }

    // PUBLIC MENU - only Available items. The public controller separately
    // ensures that the restaurant itself is Active.
    @Transactional(readOnly = true)
    public List<FoodItem> getAvailableFoodItemsByRestaurant(Long restaurantId) {
        return foodItemRepository.findByRestaurant_RestaurantIdAndAvailability(
                restaurantId, "Available");
    }

    // DELETE - refuse to destroy food referenced by any historical OrderItem.
    // Admins should mark such menu items Unavailable instead.
    @Transactional
    public void deleteFoodItem(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Food item ID is required.");
        }
        if (!foodItemRepository.existsById(id)) {
            throw new IllegalArgumentException("Food item not found.");
        }
        if (foodItemRepository.countOrderItemReferences(id) > 0) {
            throw new IllegalStateException(
                    "This food item appears in existing orders. " +
                    "Mark it Unavailable instead of deleting it."
            );
        }
        try {
            foodItemRepository.deleteById(id);
            // Forces DB constraint failures to surface before returning.
            foodItemRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException(
                    "This food item is linked to other records. " +
                    "Mark it Unavailable instead of deleting it.", ex
            );
        }
    }
}
