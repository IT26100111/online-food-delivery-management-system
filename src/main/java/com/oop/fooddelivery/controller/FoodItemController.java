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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/food")
public class FoodItemController {

    private final FoodItemService foodItemService;
    private final RestaurantOperations restaurantService;

    public FoodItemController(FoodItemService foodItemService,
                              RestaurantOperations restaurantService) {
        this.foodItemService = foodItemService;
        this.restaurantService = restaurantService;
    }

    // Do not let a browser POST bind the ID, legacy restaurant name,
    // or Restaurant association directly into the entity.
    @InitBinder("foodItem")
    public void configureBinding(WebDataBinder binder) {
        binder.setAllowedFields("foodName", "category", "price",
                "availability", "imageUrl", "description");
    }

    @ModelAttribute("restaurants")
    public List<Restaurant> getRestaurants() {
        return restaurantService.getAllRestaurants();
    }

    @GetMapping
    public String viewFoodItems(Model model) {
        model.addAttribute("foodItems", foodItemService.getAllFoodItems());
        return "food/food-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("foodItem", new FoodItem());
        return "food/add-food";
    }

    @PostMapping("/add")
    public String addFoodItem(
            @Valid @ModelAttribute("foodItem") FoodItem foodItem,
            BindingResult bindingResult,
            @RequestParam(required = false) Long restaurantId,
            Model model,
            RedirectAttributes redirectAttributes) {

        Restaurant restaurant = findValidRestaurant(restaurantId, bindingResult, null);
        if (bindingResult.hasErrors()) {
            model.addAttribute("selectedRestaurantId", restaurantId);
            return "food/add-food";
        }
        foodItem.setFoodItemId(null); // Always generate new IDs for new items.
        foodItem.setRestaurant(restaurant);
        foodItemService.saveFoodItem(foodItem);
        redirectAttributes.addFlashAttribute("foodSuccess", "Food item added successfully.");
        return "redirect:/food";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        FoodItem foodItem = foodItemService.getFoodItemById(id);
        if (foodItem == null) return "redirect:/food";
        model.addAttribute("foodItem", foodItem);
        if (foodItem.getRestaurant() != null) {
            model.addAttribute("selectedRestaurantId",
                    foodItem.getRestaurant().getRestaurantId());
        }
        return "food/edit-food";
    }

    @PostMapping("/edit/{id}")
    public String updateFoodItem(
            @PathVariable Long id,
            @Valid @ModelAttribute("foodItem") FoodItem formFood,
            BindingResult bindingResult,
            @RequestParam(required = false) Long restaurantId,
            Model model,
            RedirectAttributes redirectAttributes) {

        FoodItem existing = foodItemService.getFoodItemById(id);
        if (existing == null) return "redirect:/food";

        // An existing item may stay at its own now-inactive restaurant.
        // New assignments to other inactive restaurants are forbidden.
        Restaurant restaurant = findValidRestaurant(restaurantId, bindingResult,
                existing.getRestaurant());
        formFood.setFoodItemId(id); // Preserve edit form URL on validation errors.
        if (bindingResult.hasErrors()) {
            model.addAttribute("selectedRestaurantId", restaurantId);
            return "food/edit-food";
        }

        // Update only explicitly allowed attributes on the saved entity.
        existing.setFoodName(formFood.getFoodName());
        existing.setCategory(formFood.getCategory());
        existing.setPrice(formFood.getPrice());
        existing.setAvailability(formFood.getAvailability());
        existing.setImageUrl(formFood.getImageUrl());
        existing.setDescription(formFood.getDescription());
        existing.setRestaurant(restaurant);
        foodItemService.saveFoodItem(existing);
        redirectAttributes.addFlashAttribute("foodSuccess", "Food item updated successfully.");
        return "redirect:/food";
    }

    private Restaurant findValidRestaurant(
            Long restaurantId,
            BindingResult bindingResult,
            Restaurant existingRestaurant) {

        if (restaurantId == null) {
            bindingResult.reject("restaurant.required", "Please select a restaurant.");
            return null;
        }
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
        if (restaurant == null) {
            bindingResult.reject("restaurant.notFound", "Selected restaurant does not exist.");
            return null;
        }
        boolean keepingExistingRestaurant = existingRestaurant != null
                && restaurant.getRestaurantId().equals(existingRestaurant.getRestaurantId());
        if (!restaurant.isActive() && !keepingExistingRestaurant) {
            bindingResult.reject("restaurant.inactive",
                    "New food items can only be assigned to an Active restaurant.");
            return null;
        }
        return restaurant;
    }

    @PostMapping("/delete/{id}")
    public String deleteFoodItem(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            foodItemService.deleteFoodItem(id);
            redirectAttributes.addFlashAttribute("foodSuccess", "Food item deleted successfully.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("foodError", ex.getMessage());
        }
        return "redirect:/food";
    }
}
