package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.FoodItem;
import com.oop.fooddelivery.model.Restaurant;
import com.oop.fooddelivery.repository.FoodItemRepository;
import com.oop.fooddelivery.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock RestaurantRepository restaurantRepository;
    @Mock FoodItemRepository foodItemRepository;

    private RestaurantService service;

    @BeforeEach
    void setUp() {
        service = new RestaurantService(restaurantRepository, foodItemRepository);
    }

    private Restaurant sample() {
        return new Restaurant("Ceylon Kitchen", "Colombo 05", "0771234567",
                "Sri Lankan", "Active");
    }

    @Test
    void createDelegatesToRepository() {
        Restaurant restaurant = sample();
        when(restaurantRepository.save(restaurant)).thenReturn(restaurant);
        assertSame(restaurant, service.saveRestaurant(restaurant));
        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void readAllDelegatesToRepository() {
        Restaurant restaurant = sample();
        when(restaurantRepository.findAll()).thenReturn(List.of(restaurant));
        assertEquals(List.of(restaurant), service.getAllRestaurants());
    }

    @Test
    void readMissingRestaurantReturnsNull() {
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());
        assertNull(service.getRestaurantById(99L));
    }

    @Test
    void deleteWithoutFoodItemsSucceeds() {
        when(restaurantRepository.existsById(7L)).thenReturn(true);
        when(foodItemRepository.findByRestaurant_RestaurantId(7L)).thenReturn(List.of());
        service.deleteRestaurant(7L);
        verify(restaurantRepository).deleteById(7L);
    }

    @Test
    void deleteRestaurantWithLinkedFoodIsBlocked() {
        when(restaurantRepository.existsById(7L)).thenReturn(true);
        when(foodItemRepository.findByRestaurant_RestaurantId(7L))
                .thenReturn(List.of(new FoodItem()));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.deleteRestaurant(7L));
        assertTrue(ex.getMessage().contains("Inactive"));
        verify(restaurantRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteMissingRestaurantIsRejected() {
        when(restaurantRepository.existsById(88L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteRestaurant(88L));
        verify(restaurantRepository, never()).deleteById(anyLong());
    }

    @Test
    void nullRestaurantCannotBeSaved() {
        assertThrows(IllegalArgumentException.class,
                () -> service.saveRestaurant(null));
        verifyNoInteractions(restaurantRepository);
    }
}
