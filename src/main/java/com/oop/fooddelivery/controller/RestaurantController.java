
package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.Restaurant;
import com.oop.fooddelivery.service.RestaurantOperations;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/restaurant")
public class RestaurantController {

    private final RestaurantOperations restaurantService;

    public RestaurantController(
            RestaurantOperations restaurantService) {

        this.restaurantService = restaurantService;
    }

    // READ - List all restaurants
    @GetMapping
    public String getAllRestaurants(Model model) {

        model.addAttribute(
                "restaurants",
                restaurantService.getAllRestaurants()
        );

        return "restaurant/restaurant-list";
    }

    // CREATE - Display Add form
    @GetMapping("/add")
    public String showAddRestaurantForm(Model model) {

        model.addAttribute("restaurant", new Restaurant());

        return "restaurant/add-restaurant";
    }

    // CREATE - Save restaurant
    @PostMapping("/add")
    public String addRestaurant(
            @Valid @ModelAttribute("restaurant") Restaurant restaurant,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "restaurant/add-restaurant";
        }

        restaurantService.saveRestaurant(restaurant);

        return "redirect:/restaurant";
    }

    // UPDATE - Display Edit form
    @GetMapping("/edit/{id}")
    public String showEditRestaurantForm(
            @PathVariable Long id,
            Model model) {

        Restaurant restaurant =
                restaurantService.getRestaurantById(id);

        if (restaurant == null) {
            return "redirect:/restaurant";
        }

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("restaurantId", id);

        return "restaurant/edit-restaurant";
    }

    // UPDATE - Save edited restaurant
    @PostMapping("/edit/{id}")
    public String updateRestaurant(
            @PathVariable Long id,
            @Valid @ModelAttribute("restaurant") Restaurant formRestaurant,
            BindingResult bindingResult,
            Model model) {

        Restaurant existingRestaurant =
                restaurantService.getRestaurantById(id);

        if (existingRestaurant == null) {
            return "redirect:/restaurant";
        }

        // Preserve the URL ID when validation fails.
        model.addAttribute("restaurantId", id);

        if (bindingResult.hasErrors()) {
            return "restaurant/edit-restaurant";
        }

        // Update the existing database entity instead
        // of saving the form object directly.
        existingRestaurant.setRestaurantName(
                formRestaurant.getRestaurantName()
        );

        existingRestaurant.setAddress(
                formRestaurant.getAddress()
        );

        existingRestaurant.setPhoneNumber(
                formRestaurant.getPhoneNumber()
        );

        existingRestaurant.setCuisineType(
                formRestaurant.getCuisineType()
        );

        existingRestaurant.setStatus(
                formRestaurant.getStatus()
        );

        restaurantService.saveRestaurant(existingRestaurant);

        return "redirect:/restaurant";
    }

    // DELETE - Remove restaurant

    @PostMapping("/delete/{id}")
    public String deleteRestaurant(
            @PathVariable Long id) {

        Restaurant restaurant =
                restaurantService.getRestaurantById(id);

        if (restaurant != null) {
            restaurantService.deleteRestaurant(id);
        }

        return "redirect:/restaurant";
    }

}
