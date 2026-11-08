package com.oop.fooddelivery.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CustomerValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void validCustomerShouldPassBeanValidation() {
        Customer customer = new Customer("Amal Perera", "amal@example.com",
                "0771234567", "12 Main Street, Colombo", "Active");
        assertTrue(validator.validate(customer).isEmpty());
    }

    @Test
    void invalidCustomerShouldHaveUsefulFieldErrors() {
        Customer customer = new Customer("", "not-an-email", "123",
                "x", "Unknown");
        Set<String> invalidFields = validator.validate(customer).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
        assertTrue(invalidFields.containsAll(Set.of(
                "fullName", "email", "phoneNumber", "address", "status"
        )));
    }
}
