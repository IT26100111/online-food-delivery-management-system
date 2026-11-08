
package com.oop.fooddelivery.cart;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ShoppingCart implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int MAX_QUANTITY = 20;

    private Long restaurantId;

    // FoodItem ID -> Quantity
    private final Map<Long, Integer> items =
            new LinkedHashMap<>();

    public void addItem(
            Long selectedRestaurantId,
            Long foodItemId,
            int quantity) {

        if (selectedRestaurantId == null ||
                foodItemId == null) {
            throw new IllegalArgumentException(
                    "Restaurant and food item are required."
            );
        }

        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException(
                    "Quantity must be between 1 and 20."
            );
        }

        // Prevent mixing food from different restaurants.
        if (restaurantId != null &&
                !restaurantId.equals(selectedRestaurantId)) {
            throw new IllegalStateException(
                    "Your cart contains food from another restaurant."
            );
        }

        int existingQuantity =
                items.getOrDefault(foodItemId, 0);

        if (existingQuantity + quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException(
                    "Maximum quantity per item is 20."
            );
        }

        restaurantId = selectedRestaurantId;

        items.put(
                foodItemId,
                existingQuantity + quantity
        );
    }

    public void updateQuantity(
            Long foodItemId,
            int quantity) {

        if (!items.containsKey(foodItemId)) {
            throw new IllegalArgumentException(
                    "Food item is not in the cart."
            );
        }

        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException(
                    "Quantity must be between 1 and 20."
            );
        }

        items.put(foodItemId, quantity);
    }

    public void removeItem(Long foodItemId) {
        items.remove(foodItemId);

        if (items.isEmpty()) {
            restaurantId = null;
        }
    }

    public void clear() {
        items.clear();
        restaurantId = null;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public Map<Long, Integer> getItems() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<>(items)
        );
    }

    public int getTotalQuantity() {
        return items.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }
}
