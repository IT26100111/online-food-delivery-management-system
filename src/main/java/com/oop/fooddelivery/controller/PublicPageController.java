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

import java.util.List;

@Controller
public class PublicPageController {

    private final OrderService orderService;
    private final RestaurantOperations restaurantService;
    private final FoodItemService foodItemService;

    public PublicPageController(
            OrderService orderService,
            RestaurantOperations restaurantService,
            FoodItemService foodItemService) {
        this.orderService = orderService;
        this.restaurantService = restaurantService;
        this.foodItemService = foodItemService;
    }

    // Existing order-tracking routes remain unchanged.
    @GetMapping("/track-order")
    public String trackOrderPage() {
        return "public/track-order";
    }

    @GetMapping("/track-order/search")
    public String searchOrder(
            @RequestParam Long orderId,
            Model model) {
        Order order = orderService.getOrderById(orderId);

        if (order == null) {
            model.addAttribute("notFound", true);
            return "public/track-order";
        }

        model.addAttribute("order", order);
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
