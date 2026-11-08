
package com.oop.fooddelivery.service;

import com.oop.fooddelivery.cart.ShoppingCart;
import com.oop.fooddelivery.checkout.CheckoutForm;
import com.oop.fooddelivery.model.*;

import jakarta.validation.Validator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CheckoutService {

    private final CustomerService customerService;
    private final OrderService orderService;
    private final FoodItemService foodItemService;
    private final RestaurantOperations restaurantService;
    private final PaymentService paymentService;
    private final FeedbackAccessTokenService feedbackAccessTokenService;
    private final OrderTrackingTokenService orderTrackingTokenService;
    private final Validator validator;

    // =========================================
    // CONSTRUCTOR DEPENDENCY INJECTION
    // =========================================
    public CheckoutService(
            CustomerService customerService,
            OrderService orderService,
            FoodItemService foodItemService,
            RestaurantOperations restaurantService,
            PaymentService paymentService,
            FeedbackAccessTokenService feedbackAccessTokenService,
            OrderTrackingTokenService orderTrackingTokenService,
            Validator validator) {

        this.customerService = customerService;
        this.orderService = orderService;
        this.foodItemService = foodItemService;
        this.restaurantService = restaurantService;
        this.paymentService = paymentService;
        this.feedbackAccessTokenService = feedbackAccessTokenService;
        this.orderTrackingTokenService = orderTrackingTokenService;
        this.validator = validator;
    }

    // =========================================
    // EXISTING CHECKOUT METHOD
    // =========================================
    @Transactional
    public Order placeOrder(
            ShoppingCart cart,
            CheckoutForm form) {

        // 1. Validate customer information.
        if (form == null) {
            throw new IllegalArgumentException(
                    "Customer details are required."
            );
        }

        var violations = validator.validate(form);

        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(
                    violations.iterator().next().getMessage()
            );
        }

        // 2. Validate shopping cart.
        if (cart == null
                || cart.isEmpty()
                || cart.getRestaurantId() == null) {

            throw new IllegalStateException(
                    "Your shopping cart is empty."
            );
        }

        // 3. Validate restaurant.
        Restaurant restaurant = restaurantService
                .getRestaurantById(cart.getRestaurantId());

        if (restaurant == null || !restaurant.isActive()) {
            throw new IllegalStateException(
                    "The selected restaurant is unavailable."
            );
        }

        // 4. Validate food items.
        List<OrderItem> validatedItems = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry
                : cart.getItems().entrySet()) {

            FoodItem food = foodItemService
                    .getFoodItemById(entry.getKey());

            Integer quantity = entry.getValue();

            if (quantity == null
                    || quantity < 1
                    || quantity > 20) {

                throw new IllegalArgumentException(
                        "Invalid food quantity."
                );
            }

            if (food == null
                    || food.getRestaurant() == null
                    || !restaurant.getRestaurantId().equals(
                    food.getRestaurant().getRestaurantId())) {

                throw new IllegalStateException(
                        "A food item no longer belongs to this restaurant."
                );
            }

            if (!"Available".equals(food.getAvailability())
                    || food.getPrice() == null
                    || food.getPrice() <= 0) {

                throw new IllegalStateException(
                        food.getFoodName()
                                + " is currently unavailable."
                );
            }

            validatedItems.add(
                    new OrderItem(food, quantity)
            );
        }

        // 5. Save guest customer.
        Customer customer = new Customer(
                form.getFullName().trim(),
                form.getEmail().trim(),
                form.getPhoneNumber().trim(),
                form.getDeliveryAddress().trim(),
                "Active"
        );

        customer = customerService.saveCustomer(customer);

        // 6. Create order.
        Order order = new Order();

        order.setCustomer(customer);

        order.setRestaurantName(
                restaurant.getRestaurantName()
        );

        order.setOrderDate(LocalDate.now());
        order.setStatus(OrderStatus.PENDING);

        order.setDeliveryAddress(
                form.getDeliveryAddress().trim()
        );

        // 7. Add validated order items.
        for (OrderItem item : validatedItems) {
            order.addOrderItem(item);
        }

        // 8. Save Order and OrderItems.
        return orderService.saveOrder(order);
    }

    // =========================================
    // CHECKOUT WITH PAYMENT
    // =========================================
    @Transactional
    public Order placeOrderWithPayment(
            ShoppingCart cart,
            CheckoutForm form,
            PaymentMethod paymentMethod) {

        // Payment method is mandatory.
        if (paymentMethod == null) {
            throw new IllegalArgumentException(
                    "Please select a payment method."
            );
        }

        // Reuse validated order creation.
        Order savedOrder = placeOrder(cart, form);

        // Create linked payment in the same transaction.
        paymentService.createPayment(
                savedOrder.getOrderId(),
                paymentMethod
        );

        return savedOrder;
    }

    // =========================================
    // CHECKOUT WITH PAYMENT, REVIEW TOKEN
    // AND SECURE TRACKING TOKEN
    // =========================================
    @Transactional
    public CheckoutWithReviewToken
    placeOrderWithPaymentAndReviewToken(
            ShoppingCart cart,
            CheckoutForm form,
            PaymentMethod paymentMethod) {

        // 1. Create customer, order, order items
        // and payment using the existing workflow.
        Order savedOrder = placeOrderWithPayment(
                cart,
                form,
                paymentMethod
        );

        // 2. Generate a secure order tracking token.
        // Only the SHA-256 hash is stored in MySQL.
        String trackingToken =
                orderTrackingTokenService
                        .createTrackingToken(savedOrder);

        // 3. Generate the private customer review token.
        // Only its hash is stored in MySQL.
        String rawReviewToken =
                feedbackAccessTokenService
                        .generateTokenForOrder(savedOrder);

        // 4. Return the saved order and both raw tokens
        // to CheckoutController.
        //
        // The Controller will store the tokens in
        // the customer's browser session.
        return new CheckoutWithReviewToken(
                savedOrder,
                rawReviewToken,
                trackingToken
        );
    }

    // =========================================
    // CHECKOUT RESULT
    // =========================================
    public record CheckoutWithReviewToken(
            Order order,
            String reviewToken,
            String trackingToken
    ) {
    }
}
