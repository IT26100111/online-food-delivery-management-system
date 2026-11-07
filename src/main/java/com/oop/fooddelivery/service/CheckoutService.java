
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
    private final Validator validator;

    public CheckoutService(
            CustomerService customerService,
            OrderService orderService,
            FoodItemService foodItemService,
            RestaurantOperations restaurantService,
            PaymentService paymentService,
            FeedbackAccessTokenService feedbackAccessTokenService,
            Validator validator) {

        this.customerService = customerService;
        this.orderService = orderService;
        this.foodItemService = foodItemService;
        this.restaurantService = restaurantService;
        this.paymentService = paymentService;
        this.feedbackAccessTokenService = feedbackAccessTokenService;
        this.validator = validator;
    }

    // =========================================
    // EXISTING CHECKOUT METHOD
    // Retained until the Controller is migrated.
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
    // NEW: CHECKOUT WITH PAYMENT
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

        // Reuse existing validated order creation.
        Order savedOrder = placeOrder(cart, form);

        // Create linked payment in the same transaction.
        paymentService.createPayment(
                savedOrder.getOrderId(),
                paymentMethod
        );

        return savedOrder;
    }
    // New checkout flow: order, payment and private review token are
    // persisted together. Any failure rolls back all three records.
    @Transactional
    public CheckoutWithReviewToken placeOrderWithPaymentAndReviewToken(
            ShoppingCart cart,
            CheckoutForm form,
            PaymentMethod paymentMethod) {

        // Reuse the existing validated order/payment workflow.
        Order savedOrder = placeOrderWithPayment(cart, form, paymentMethod);

        // Only the SHA-256 hash is stored in the database.
        String rawReviewToken =
                feedbackAccessTokenService.generateTokenForOrder(savedOrder);

        // The raw token is returned only to the checkout controller,
        // which will put it in the customer's private browser session.
        return new CheckoutWithReviewToken(savedOrder, rawReviewToken);
    }

    public record CheckoutWithReviewToken(Order order, String reviewToken) {
    }
}
