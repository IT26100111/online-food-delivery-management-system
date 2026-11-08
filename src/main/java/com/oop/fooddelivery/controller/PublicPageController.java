package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.FoodItem;
import com.oop.fooddelivery.model.Order;
import com.oop.fooddelivery.model.Restaurant;
import com.oop.fooddelivery.service.FoodItemService;
import com.oop.fooddelivery.service.OrderService;
import com.oop.fooddelivery.service.RestaurantOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.oop.fooddelivery.service.OrderTrackingTokenService;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

@Controller
public class PublicPageController {

    private final OrderService orderService;
    private final RestaurantOperations restaurantService;
    private final FoodItemService foodItemService;
    private final OrderTrackingTokenService orderTrackingTokenService;

    public PublicPageController(
            OrderService orderService,
            RestaurantOperations restaurantService,
            FoodItemService foodItemService,
            OrderTrackingTokenService orderTrackingTokenService) {

        this.orderService = orderService;
        this.restaurantService = restaurantService;
        this.foodItemService = foodItemService;
        this.orderTrackingTokenService = orderTrackingTokenService;
    }

    // Existing order-tracking routes remain unchanged.
    @GetMapping("/track-order")
    public String trackOrderPage() {
        return "public/track-order";
    }


    // =========================================
    // DISABLE INSECURE ORDER ID TRACKING
    // =========================================
    @GetMapping("/track-order/search")
    public String searchOrder() {

        // Numerical Order IDs must not grant
        // access to another customer's order.
        // Direct visitors to private-link tracking.
        return "redirect:/track-order";
    }



    // =========================================
    // SECURE GUEST ORDER TRACKING
    // =========================================
    @GetMapping("/track-order/secure")
    public String secureTrackOrder(
            @RequestParam(required = false) String token,
            Model model,
            HttpServletResponse response) {

        // Do not let browsers or intermediaries cache
        // pages accessed using private tracking tokens.
        response.setHeader(
                "Cache-Control",
                "no-store"
        );

        // Avoid sending the private token URL
        // as a referrer to other websites.
        response.setHeader(
                "Referrer-Policy",
                "no-referrer"
        );

        // Look up the order using the token hash.
        // Invalid or expired tokens return no order.
        var order = orderTrackingTokenService
                .findOrderByToken(token);

        if (order.isEmpty()) {
            model.addAttribute("notFound", true);
            return "public/track-order";
        }

        // The tracking page displays order status
        // without exposing private customer details.
        model.addAttribute("order", order.get());

        return "public/track-order";
    }

    // Public restaurant directory: only active restaurants.
    @GetMapping("/restaurants")
    public String restaurants(Model model) {
        List<Restaurant> activeRestaurants = restaurantService
                .getAllRestaurants()
                .stream()
                .filter(Restaurant::isActive)
                .toList();

        model.addAttribute("restaurants", activeRestaurants);
        return "public/restaurants";
    }

    // /menu displays food from all active restaurants.
    // /menu?restaurantId=ID displays food from one active restaurant.
    @GetMapping("/menu")
    public String menu(
            @RequestParam(required = false) Long restaurantId,
            Model model) {

        List<Restaurant> activeRestaurants = restaurantService
                .getAllRestaurants()
                .stream()
                .filter(Restaurant::isActive)
                .toList();

        model.addAttribute("restaurants", activeRestaurants);

        if (restaurantId != null) {
            Restaurant selectedRestaurant =
                    restaurantService.getRestaurantById(restaurantId);

            // Do not show hidden or non-existent restaurant menus.
            if (selectedRestaurant == null || !selectedRestaurant.isActive()) {
                return "redirect:/restaurants";
            }

            model.addAttribute("selectedRestaurant", selectedRestaurant);
            model.addAttribute(
                    "foodItems",
                    foodItemService.getAvailableFoodItemsByRestaurant(restaurantId)
            );
        } else {
            // Using active restaurant IDs here also prevents showing food
            // from inactive restaurants on the general Food page.
            List<FoodItem> availableFood = activeRestaurants
                    .stream()
                    .flatMap(restaurant -> foodItemService
                            .getAvailableFoodItemsByRestaurant(restaurant.getRestaurantId())
                            .stream())
                    .toList();

            model.addAttribute("selectedRestaurant", null);
            model.addAttribute("foodItems", availableFood);
        }

        return "public/menu";
    }

    @GetMapping("/recommendations")
    public String recommendations() {
        return "public/recommendations";
    }

    @GetMapping("/about")
    public String about() {
        return "public/about";
    }
}
