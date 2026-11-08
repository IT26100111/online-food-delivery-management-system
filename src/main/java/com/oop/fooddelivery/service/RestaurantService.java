package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.Restaurant;
import com.oop.fooddelivery.repository.FoodItemRepository;
import com.oop.fooddelivery.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class RestaurantService implements RestaurantOperations {

    private final RestaurantRepository restaurantRepository;
    private final FoodItemRepository foodItemRepository;

    // Constructor injection: Spring supplies the repository implementations.
    public RestaurantService(RestaurantRepository restaurantRepository,
                             FoodItemRepository foodItemRepository) {
        this.restaurantRepository = Objects.requireNonNull(restaurantRepository);
        this.foodItemRepository = Objects.requireNonNull(foodItemRepository);
    }

    @Override
    @Transactional
    public Restaurant saveRestaurant(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant details are required.");
        }
        return restaurantRepository.save(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Restaurant getRestaurantById(Long id) {
        if (id == null) return null;
        return restaurantRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void deleteRestaurant(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Restaurant ID is required.");
        }

        if (!restaurantRepository.existsById(id)) {
            throw new IllegalArgumentException("Restaurant not found.");
        }

        // An existing FoodItem has a JPA foreign-key relationship to Restaurant.
        // Do not delete a restaurant while menu items refer to it. This preserves
        // restaurant/food associations and any order-item history.
        if (!foodItemRepository.findByRestaurant_RestaurantId(id).isEmpty()) {
            throw new IllegalStateException(
                    "This restaurant still has food items. Set it to Inactive " +
                    "instead of deleting it, or resolve the linked menu items first."
            );
        }

        restaurantRepository.deleteById(id);
    }
}
