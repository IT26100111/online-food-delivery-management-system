package com.oop.fooddelivery.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FoodItemValidationTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    private FoodItem validFood() {
        return new FoodItem(
                "Chicken Kottu", "Test Restaurant", "Sri Lankan", 1250.00,
                "Available", "https://example.com/kottu.jpg", "Freshly prepared kottu roti"
        );
    }

    @Test
    void completeFoodItemPassesValidation() {
        assertTrue(validator.validate(validFood()).isEmpty());
    }

    @Test
    void blankNameFailsValidation() {
        FoodItem food = validFood();
        food.setFoodName("  ");
        assertFalse(validator.validate(food).isEmpty());
    }

    @Test
    void zeroPriceFailsValidation() {
        FoodItem food = validFood();
        food.setPrice(0.0);
        assertFalse(validator.validate(food).isEmpty());
    }

    @Test
    void missingPriceFailsValidation() {
        FoodItem food = validFood();
        food.setPrice(null);
        assertFalse(validator.validate(food).isEmpty());
    }

    @Test
    void invalidAvailabilityFailsValidation() {
        FoodItem food = validFood();
        food.setAvailability("Sold Out Forever");
        assertFalse(validator.validate(food).isEmpty());
    }

    @Test
    void overlongDescriptionFailsValidation() {
        FoodItem food = validFood();
        food.setDescription("x".repeat(1001));
        assertFalse(validator.validate(food).isEmpty());
    }
}
