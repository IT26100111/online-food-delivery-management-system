
package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.cart.ShoppingCart;
import com.oop.fooddelivery.model.FoodItem;
import com.oop.fooddelivery.model.Restaurant;
import com.oop.fooddelivery.service.FoodItemService;
import com.oop.fooddelivery.service.RestaurantOperations;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@SessionAttributes("shoppingCart")
public class CartController {

    private final FoodItemService foodItemService;
    private final RestaurantOperations restaurantService;

    public CartController(
            FoodItemService foodItemService,
            RestaurantOperations restaurantService) {

        this.foodItemService = foodItemService;
        this.restaurantService = restaurantService;
    }

    // Create a separate cart for each browser session.
    @ModelAttribute("shoppingCart")
    public ShoppingCart createShoppingCart() {
        return new ShoppingCart();
    }

    // VIEW CART
    @GetMapping
    public String viewCart(
            @ModelAttribute("shoppingCart") ShoppingCart cart,
            Model model) {

        List<CartLine> cartLines = new ArrayList<>();
        double totalAmount = 0.0;
        boolean containsUnavailableItems = false;

        Restaurant selectedRestaurant = null;

        if (cart.getRestaurantId() != null) {
            selectedRestaurant = restaurantService
                    .getRestaurantById(cart.getRestaurantId());
        }

        boolean restaurantActive =
                selectedRestaurant != null
                        && selectedRestaurant.isActive();

        for (Map.Entry<Long, Integer> entry
                : cart.getItems().entrySet()) {

            FoodItem food = foodItemService
                    .getFoodItemById(entry.getKey());

            if (food == null
                    || food.getRestaurant() == null
                    || !cart.getRestaurantId().equals(
                    food.getRestaurant().getRestaurantId())
                    || !"Available".equals(food.getAvailability())
                    || food.getPrice() == null
                    || food.getPrice() <= 0
                    || !restaurantActive) {

                containsUnavailableItems = true;
                continue;
            }

            int quantity = entry.getValue();

            double subtotal = food.getPrice() * quantity;

            cartLines.add(
                    new CartLine(food, quantity, subtotal)
            );

            totalAmount += subtotal;
        }

        model.addAttribute("cartLines", cartLines);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("cartCount", cart.getTotalQuantity());
        model.addAttribute("selectedRestaurant", selectedRestaurant);
        model.addAttribute(
                "containsUnavailableItems",
                containsUnavailableItems
        );

        return "public/cart";
    }

    // ADD FOOD TO CART
    @PostMapping("/add")
    public String addToCart(
            @RequestParam Long foodItemId,
            @RequestParam(defaultValue = "1") int quantity,
            @ModelAttribute("shoppingCart") ShoppingCart cart,
            RedirectAttributes redirectAttributes) {

        FoodItem food = foodItemService.getFoodItemById(foodItemId);

        if (food == null || food.getRestaurant() == null) {
            redirectAttributes.addFlashAttribute(
                    "cartError",
                    "Food item was not found."
            );
            return "redirect:/cart";
        }

        Long restaurantId = food.getRestaurant()
                .getRestaurantId();

        Restaurant restaurant = restaurantService
                .getRestaurantById(restaurantId);

        if (restaurant == null || !restaurant.isActive()) {
            redirectAttributes.addFlashAttribute(
                    "cartError",
                    "This restaurant is not currently active."
            );
            return "redirect:/cart";
        }

        if (!"Available".equals(food.getAvailability())
                || food.getPrice() == null
                || food.getPrice() <= 0) {

            redirectAttributes.addFlashAttribute(
                    "cartError",
                    "This food item is currently unavailable."
            );
            return "redirect:/cart";
        }

        try {
            cart.addItem(
                    restaurantId,
                    foodItemId,
                    quantity
            );

            redirectAttributes.addFlashAttribute(
                    "cartSuccess",
                    food.getFoodName() + " added to your cart."
            );

        } catch (IllegalArgumentException
                 | IllegalStateException exception) {

            redirectAttributes.addFlashAttribute(
                    "cartError",
                    exception.getMessage()
            );
        }

        return "redirect:/cart";
    }

    // UPDATE ITEM QUANTITY
    @PostMapping("/update")
    public String updateQuantity(
            @RequestParam Long foodItemId,
            @RequestParam int quantity,
            @ModelAttribute("shoppingCart") ShoppingCart cart,
            RedirectAttributes redirectAttributes) {

        try {
            cart.updateQuantity(foodItemId, quantity);

            redirectAttributes.addFlashAttribute(
                    "cartSuccess",
                    "Cart quantity updated."
            );

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "cartError",
                    exception.getMessage()
            );
        }

        return "redirect:/cart";
    }

    // REMOVE SINGLE ITEM
    @PostMapping("/remove")
    public String removeItem(
            @RequestParam Long foodItemId,
            @ModelAttribute("shoppingCart") ShoppingCart cart,
            RedirectAttributes redirectAttributes) {

        cart.removeItem(foodItemId);

        redirectAttributes.addFlashAttribute(
                "cartSuccess",
                "Item removed from your cart."
        );

        return "redirect:/cart";
    }

    // CLEAR ENTIRE CART
    @PostMapping("/clear")
    public String clearCart(
            @ModelAttribute("shoppingCart") ShoppingCart cart,
            RedirectAttributes redirectAttributes) {

        cart.clear();

        redirectAttributes.addFlashAttribute(
                "cartSuccess",
                "Your cart has been cleared."
        );

        return "redirect:/cart";
    }

    // Data object for displaying cart items in Thymeleaf.
    public static class CartLine {

        private final FoodItem foodItem;
        private final int quantity;
        private final double subtotal;

        public CartLine(
                FoodItem foodItem,
                int quantity,
                double subtotal) {

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
