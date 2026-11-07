package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.cart.ShoppingCart;
import com.oop.fooddelivery.checkout.CheckoutForm;
import com.oop.fooddelivery.model.FoodItem;
import com.oop.fooddelivery.model.Order;
import com.oop.fooddelivery.model.Payment;
import com.oop.fooddelivery.model.PaymentMethod;
import com.oop.fooddelivery.model.Restaurant;
import com.oop.fooddelivery.service.CheckoutService;
import com.oop.fooddelivery.service.FoodItemService;
import com.oop.fooddelivery.service.OrderService;
import com.oop.fooddelivery.service.PaymentService;
import com.oop.fooddelivery.service.RestaurantOperations;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final FoodItemService foodItemService;
    private final RestaurantOperations restaurantService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public CheckoutController(
            CheckoutService checkoutService,
            FoodItemService foodItemService,
            RestaurantOperations restaurantService,
            OrderService orderService,
            PaymentService paymentService) {

        this.checkoutService = checkoutService;
        this.foodItemService = foodItemService;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    // Display checkout form and current server-calculated order summary.
    @GetMapping
    public String showCheckout(
            @SessionAttribute(name = "shoppingCart", required = false)
            ShoppingCart cart,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (cart == null || cart.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "cartError",
                    "Add some food to your cart before checkout."
            );
            return "redirect:/cart";
        }

        if (!prepareOrderSummary(cart, model)) {
            redirectAttributes.addFlashAttribute(
                    "cartError",
                    "Some cart items are unavailable. Please review your cart."
            );
            return "redirect:/cart";
        }

        model.addAttribute("checkoutForm", new CheckoutForm());
        model.addAttribute("selectedPaymentMethod", "");
        return "public/checkout";
    }

    // Save a pending Order and its linked pending Payment in one transaction.
    @PostMapping
    public String placeOrder(
            @Valid @ModelAttribute("checkoutForm") CheckoutForm checkoutForm,
            BindingResult bindingResult,
            @RequestParam(name = "paymentMethod", required = false)
            String paymentMethod,
            @SessionAttribute(name = "shoppingCart", required = false)
            ShoppingCart cart,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (cart == null || cart.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "cartError",
                    "Your shopping cart is empty."
            );
            return "redirect:/cart";
        }

        if (!prepareOrderSummary(cart, model)) {
            redirectAttributes.addFlashAttribute(
                    "cartError",
                    "Your cart has changed. Please review the items."
            );
            return "redirect:/cart";
        }

        // Keep the user's choice if field validation fails.
        model.addAttribute("selectedPaymentMethod", paymentMethod);

        PaymentMethod selectedMethod = null;
        try {
            selectedMethod = PaymentMethod.valueOf(
                    paymentMethod == null ? "" : paymentMethod
            );
        } catch (IllegalArgumentException ex) {
            bindingResult.reject(
                    "payment.invalid",
                    "Please select Cash on Delivery or Card Simulation."
            );
        }

        if (bindingResult.hasErrors()) {
            return "public/checkout";
        }

        try {
            // CheckoutService saves Customer, Order, OrderItems, Payment
            // and the private review token in a single transaction.
            CheckoutService.CheckoutWithReviewToken result =
                    checkoutService.placeOrderWithPaymentAndReviewToken(
                            cart,
                            checkoutForm,
                            selectedMethod
                    );

            // Keep the confirmation and the raw token in this browser
            // session only. Do not write the raw token to the database.
            session.setAttribute(
                    "checkoutConfirmedOrderId",
                    result.order().getOrderId()
            );
            session.setAttribute(
                    "checkoutReviewToken",
                    result.reviewToken()
            );

            // Clear only after the transactional checkout succeeds.
            cart.clear();
            return "redirect:/checkout/success";

        } catch (IllegalArgumentException | IllegalStateException ex) {
            bindingResult.reject("checkout.error", ex.getMessage());
            return "public/checkout";
        }
    }

    @GetMapping("/success")
    public String orderSuccess(
            HttpSession session,
            HttpServletResponse response,
            Model model) {

        // Confirmation includes a private bearer link; do not cache it.
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");

        Object storedId = session.getAttribute("checkoutConfirmedOrderId");

        if (!(storedId instanceof Long orderId)) {
            return "redirect:/menu";
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            session.removeAttribute("checkoutConfirmedOrderId");
            session.removeAttribute("checkoutReviewToken");
            return "redirect:/menu";
        }

        // Only the order ID saved in this session can be confirmed.
        // Fetch its actual Payment instead of displaying hardcoded status.
        Payment payment = paymentService.getLatestPaymentForOrder(orderId);

        model.addAttribute("order", order);
        model.addAttribute("payment", payment);
        model.addAttribute("reviewToken", session.getAttribute("checkoutReviewToken"));
        return "public/order-confirmation";
    }

    private boolean prepareOrderSummary(ShoppingCart cart, Model model) {
        if (cart == null || cart.isEmpty() || cart.getRestaurantId() == null) {
            return false;
        }

        Restaurant restaurant = restaurantService
                .getRestaurantById(cart.getRestaurantId());

        if (restaurant == null || !restaurant.isActive()) {
            return false;
        }

        List<FoodItem> availableFoods = foodItemService
                .getAvailableFoodItemsByRestaurant(restaurant.getRestaurantId());

        Map<Long, FoodItem> foodById = availableFoods.stream()
                .collect(Collectors.toMap(FoodItem::getFoodItemId, food -> food));

        List<CheckoutLine> cartLines = new ArrayList<>();
        double totalAmount = 0.0;

        for (Map.Entry<Long, Integer> entry : cart.getItems().entrySet()) {
            FoodItem food = foodById.get(entry.getKey());
            Integer quantity = entry.getValue();

            if (food == null || quantity == null || quantity < 1
                    || quantity > 20 || food.getPrice() == null
                    || food.getPrice() <= 0) {
                return false;
            }

            double subtotal = food.getPrice() * quantity;
            cartLines.add(new CheckoutLine(food, quantity, subtotal));
            totalAmount += subtotal;
        }

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("cartLines", cartLines);
        model.addAttribute("totalAmount", totalAmount);
        return true;
    }

    public static class CheckoutLine {
        private final FoodItem foodItem;
        private final int quantity;
        private final double subtotal;

        public CheckoutLine(FoodItem foodItem, int quantity, double subtotal) {
            this.foodItem = foodItem;
            this.quantity = quantity;
            this.subtotal = subtotal;
        }

        public FoodItem getFoodItem() {
            return foodItem;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getSubtotal() {
            return subtotal;
        }
    }
}
