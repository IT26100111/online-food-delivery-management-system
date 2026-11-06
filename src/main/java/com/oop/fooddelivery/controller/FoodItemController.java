
package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.FoodItem;
import com.oop.fooddelivery.model.Restaurant;
import com.oop.fooddelivery.service.FoodItemService;
import com.oop.fooddelivery.service.RestaurantOperations;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/food")
public class FoodItemController {

    private final FoodItemService foodItemService;
    private final RestaurantOperations restaurantService;

    public FoodItemController(
            FoodItemService foodItemService,
            RestaurantOperations restaurantService) {

        this.foodItemService = foodItemService;
        this.restaurantService = restaurantService;
    }

    // Restrict direct binding to editable food properties.
    @InitBinder("foodItem")
    public void configureBinding(WebDataBinder binder) {
        binder.setAllowedFields(
                "foodName",
                "category",
                "price",
                "availability",
                "imageUrl",
                "description"
        );
    }

    // Load restaurants for Add and Edit forms.
    @ModelAttribute("restaurants")
    public List<Restaurant> getRestaurants() {
        return restaurantService.getAllRestaurants();
    }

    // READ - Food Item List
    @GetMapping
    public String viewFoodItems(Model model) {

        model.addAttribute(
                "foodItems",
                foodItemService.getAllFoodItems()
        );

        return "food/food-list";
    }

    // CREATE - Show Add form
    @GetMapping("/add")
    public String showAddForm(Model model) {

        model.addAttribute("foodItem", new FoodItem());

        return "food/add-food";
    }

    // CREATE - Save FoodItem with real Restaurant
    @PostMapping("/add")
    public String addFoodItem(
            @Valid @ModelAttribute("foodItem") FoodItem foodItem,
            BindingResult bindingResult,
            @RequestParam(required = false) Long restaurantId,
            Model model) {

        Restaurant restaurant = findValidRestaurant(
                restaurantId,
                bindingResult
        );

        if (bindingResult.hasErrors()) {
            model.addAttribute("selectedRestaurantId", restaurantId);
            return "food/add-food";
        }

        // A new FoodItem must receive a generated ID.
        foodItem.setFoodItemId(null);

        // Save the real association.
        // Also synchronizes the legacy restaurantName.
        foodItem.setRestaurant(restaurant);

        foodItemService.saveFoodItem(foodItem);

        return "redirect:/food";
    }

    // UPDATE - Show Edit form
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        FoodItem foodItem = foodItemService.getFoodItemById(id);

        if (foodItem == null) {
            return "redirect:/food";
        }

        model.addAttribute("foodItem", foodItem);

        if (foodItem.getRestaurant() != null) {
            model.addAttribute(
                    "selectedRestaurantId",
                    foodItem.getRestaurant().getRestaurantId()
            );
        }

        return "food/edit-food";
    }

    // UPDATE - Save changes to existing FoodItem
    @PostMapping("/edit/{id}")
    public String updateFoodItem(
            @PathVariable Long id,
            @Valid @ModelAttribute("foodItem") FoodItem formFood,
            BindingResult bindingResult,
            @RequestParam(required = false) Long restaurantId,
            Model model) {

        FoodItem existingFood = foodItemService.getFoodItemById(id);

        if (existingFood == null) {
            return "redirect:/food";
        }

        Restaurant restaurant = findValidRestaurant(
                restaurantId,
                bindingResult
        );

        // Preserve the ID if the form must be displayed again.
        formFood.setFoodItemId(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("selectedRestaurantId", restaurantId);
            return "food/edit-food";
        }

        // Update the original persistent record safely.
        existingFood.setFoodName(formFood.getFoodName());
        existingFood.setCategory(formFood.getCategory());
        existingFood.setPrice(formFood.getPrice());
        existingFood.setAvailability(formFood.getAvailability());
        existingFood.setImageUrl(formFood.getImageUrl());
        existingFood.setDescription(formFood.getDescription());

        // Update real Restaurant association.
        existingFood.setRestaurant(restaurant);

        foodItemService.saveFoodItem(existingFood);

        return "redirect:/food";
    }

    // Validate Restaurant selection against the database.
    private Restaurant findValidRestaurant(
            Long restaurantId,
            BindingResult bindingResult) {

        if (restaurantId == null) {
            bindingResult.reject(
                    "restaurant.required",
                    "Please select a restaurant."
            );
            return null;
        }

        Restaurant restaurant =
                restaurantService.getRestaurantById(restaurantId);

        if (restaurant == null) {
            bindingResult.reject(
                    "restaurant.notFound",
                    "Selected restaurant does not exist."
            );
            return null;
        }

        if (!restaurant.isActive()) {
            bindingResult.reject(
                    "restaurant.inactive",
                    "Selected restaurant is inactive."
            );
            return null;
        }

        return restaurant;
    }

    // Legacy delete route - will be secured separately.
    @GetMapping("/delete/{id}")
    public String deleteFoodItem(@PathVariable Long id) {

        foodItemService.deleteFoodItem(id);

        return "redirect:/food";
    }
}
