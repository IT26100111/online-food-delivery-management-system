package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.FoodItem;
import com.oop.fooddelivery.repository.FoodItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodItemServiceTest {

    @Mock
    private FoodItemRepository repository;

    private FoodItemService service;

    @BeforeEach
    void setUp() {
        service = new FoodItemService(repository);
    }

    @Test
    void saveFoodItemDelegatesToRepository() {
        FoodItem food = mock(FoodItem.class);
        when(repository.save(food)).thenReturn(food);
        assertSame(food, service.saveFoodItem(food));
        verify(repository).save(food);
    }

    @Test
    void saveRejectsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.saveFoodItem(null));
        verifyNoInteractions(repository);
    }

    @Test
    void publicMenuIncludesOnlyAvailableFood() {
        FoodItem rice = mock(FoodItem.class);
        when(repository.findByRestaurant_RestaurantIdAndAvailability(7L, "Available"))
                .thenReturn(List.of(rice));

        assertEquals(List.of(rice), service.getAvailableFoodItemsByRestaurant(7L));
        verify(repository).findByRestaurant_RestaurantIdAndAvailability(7L, "Available");
    }

    @Test
    void readMissingFoodReturnsNull() {
        when(repository.findById(888L)).thenReturn(Optional.empty());
        assertNull(service.getFoodItemById(888L));
        assertNull(service.getFoodItemById(null));
    }

    @Test
    void deleteUnreferencedFoodSucceeds() {
        when(repository.existsById(8L)).thenReturn(true);
        when(repository.countOrderItemReferences(8L)).thenReturn(0L);

        service.deleteFoodItem(8L);

        verify(repository).deleteById(8L);
        verify(repository).flush();
    }

    @Test
    void deleteFoodUsedInOrderIsRejected() {
        when(repository.existsById(9L)).thenReturn(true);
        when(repository.countOrderItemReferences(9L)).thenReturn(3L);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.deleteFoodItem(9L));
        assertTrue(exception.getMessage().contains("Unavailable"));
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void deleteUnknownFoodIsRejected() {
        when(repository.existsById(10L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteFoodItem(10L));
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void deleteNullIdIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteFoodItem(null));
        verifyNoInteractions(repository);
    }

    @Test
    void databaseConstraintFailureDisplaysSafeError() {
        when(repository.existsById(11L)).thenReturn(true);
        when(repository.countOrderItemReferences(11L)).thenReturn(0L);
        doThrow(new DataIntegrityViolationException("FK constraint"))
                .when(repository).flush();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.deleteFoodItem(11L));
        assertTrue(ex.getMessage().contains("linked"));
    }
}
