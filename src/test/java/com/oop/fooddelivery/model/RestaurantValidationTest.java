package com.oop.fooddelivery.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantValidationTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    private Restaurant validRestaurant() {
        return new Restaurant("Ceylon Kitchen", "42 Main Street, Colombo",
                "0771234567", "Sri Lankan", "Active");
    }

    @Test
    void validRestaurantHasNoViolations() {
        assertTrue(validator.validate(validRestaurant()).isEmpty());
    }

    @Test
    void blankRestaurantNameIsRejected() {
        Restaurant restaurant = validRestaurant();
        restaurant.setRestaurantName("  ");
        assertTrue(validator.validate(restaurant).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("restaurantName")));
    }

    @Test
    void invalidPhoneIsRejected() {
        Restaurant restaurant = validRestaurant();
        restaurant.setPhoneNumber("123");
        assertTrue(validator.validate(restaurant).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber")));
    }

    @Test
    void unexpectedStatusIsRejected() {
        Restaurant restaurant = validRestaurant();
        restaurant.setStatus("Deleted");
        assertTrue(validator.validate(restaurant).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }

    @Test
    void overlyLongCuisineIsRejected() {
        Restaurant restaurant = validRestaurant();
        restaurant.setCuisineType("X".repeat(61));
        assertTrue(validator.validate(restaurant).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("cuisineType")));
    }
}
