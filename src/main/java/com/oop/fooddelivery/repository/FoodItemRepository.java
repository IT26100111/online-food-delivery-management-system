package com.oop.fooddelivery.repository;

import com.oop.fooddelivery.model.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    // Food items associated with one restaurant.
    List<FoodItem> findByRestaurant_RestaurantId(Long restaurantId);

    // Only items explicitly marked Available are shown on the public menu.
    List<FoodItem> findByRestaurant_RestaurantIdAndAvailability(
            Long restaurantId,
            String availability
    );

    // OrderItem has a ManyToOne relationship to FoodItem. We must preserve
    // historic order lines instead of deleting the food they refer to.
    @Query("select count(oi) from OrderItem oi " +
           "where oi.foodItem.foodItemId = :foodItemId")
    long countOrderItemReferences(@Param("foodItemId") Long foodItemId);
}
