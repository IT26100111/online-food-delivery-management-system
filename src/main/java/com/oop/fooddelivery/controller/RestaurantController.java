package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.Restaurant;
import com.oop.fooddelivery.service.RestaurantOperations;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/restaurant")
public class RestaurantController {

    private final RestaurantOperations restaurantService;

    public RestaurantController(RestaurantOperations restaurantService) {
        this.restaurantService = restaurantService;
    }

    // Browser requests may edit business fields, not the generated restaurant ID.
    @InitBinder("restaurant")
    public void bindEditableRestaurantFields(WebDataBinder binder) {
        binder.setAllowedFields(
                "restaurantName", "address", "phoneNumber", "cuisineType", "status"
        );
    }

    // READ: administrator's restaurant list.
    @GetMapping
    public String getAllRestaurants(Model model) {
        model.addAttribute("restaurants", restaurantService.getAllRestaurants());
        return "restaurant/restaurant-list";
    }

    // CREATE: blank form.
    @GetMapping("/add")
    public String showAddRestaurantForm(Model model) {
        model.addAttribute("restaurant", new Restaurant());
        return "restaurant/add-restaurant";
    }

    // CREATE: validation occurs before saving.
    @PostMapping("/add")
    public String addRestaurant(
            @Valid @ModelAttribute("restaurant") Restaurant restaurant,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "restaurant/add-restaurant";
        }

        restaurantService.saveRestaurant(restaurant);
        redirectAttributes.addFlashAttribute("success", "Restaurant added successfully.");
        return "redirect:/restaurant";
    }

    // UPDATE: show saved values.
    @GetMapping("/edit/{id}")
    public String showEditRestaurantForm(@PathVariable Long id, Model model) {
        Restaurant restaurant = restaurantService.getRestaurantById(id);
        if (restaurant == null) return "redirect:/restaurant";

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("restaurantId", id);
        return "restaurant/edit-restaurant";
    }

    // UPDATE: change only allowed fields on the existing persistent entity.
    @PostMapping("/edit/{id}")
    public String updateRestaurant(
            @PathVariable Long id,
            @Valid @ModelAttribute("restaurant") Restaurant formRestaurant,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        Restaurant existingRestaurant = restaurantService.getRestaurantById(id);
        if (existingRestaurant == null) return "redirect:/restaurant";

        model.addAttribute("restaurantId", id);
        if (bindingResult.hasErrors()) {
            return "restaurant/edit-restaurant";
        }

        existingRestaurant.setRestaurantName(formRestaurant.getRestaurantName());
        existingRestaurant.setAddress(formRestaurant.getAddress());
        existingRestaurant.setPhoneNumber(formRestaurant.getPhoneNumber());
        existingRestaurant.setCuisineType(formRestaurant.getCuisineType());
        existingRestaurant.setStatus(formRestaurant.getStatus());

        restaurantService.saveRestaurant(existingRestaurant);
        redirectAttributes.addFlashAttribute("success", "Restaurant updated successfully.");
        return "redirect:/restaurant";
    }

    // DELETE: use POST with CSRF. Reject deleting restaurants with linked food.
    @PostMapping("/delete/{id}")
    public String deleteRestaurant(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            restaurantService.deleteRestaurant(id);
            redirectAttributes.addFlashAttribute("success", "Restaurant deleted successfully.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/restaurant";
    }
}
