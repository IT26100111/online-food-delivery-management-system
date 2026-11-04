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

    public RestaurantController(RestaurantOperations restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public String getAllRestaurants(Model model) {
        model.addAttribute(
                "restaurants",
                restaurantService.getAllRestaurants()
        );

        return "restaurant/restaurant-list";
    }

    @GetMapping("/add")
    public String showAddRestaurantForm(Model model) {
        model.addAttribute("restaurant", new Restaurant());

        return "restaurant/add-restaurant";
    }

    @PostMapping("/add")
    public String addRestaurant(
            @Valid @ModelAttribute("restaurant") Restaurant restaurant,
            BindingResult bindingResult) {

        if (!restaurant.hasValidStatus()) {
            bindingResult.rejectValue(
                    "status",
                    "invalid.status",
                    "Status must be Active or Inactive"
            );
        }

        if (bindingResult.hasErrors()) {
            return "restaurant/add-restaurant";
        }

        restaurantService.saveRestaurant(restaurant);

        return "redirect:/restaurant";
    }

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

        return "restaurant/edit-restaurant";
    }

    @PostMapping("/edit/{id}")
    public String updateRestaurant(
            @PathVariable Long id,
            @Valid @ModelAttribute("restaurant") Restaurant formRestaurant,
            BindingResult bindingResult) {

        if (!formRestaurant.hasValidStatus()) {
            bindingResult.rejectValue(
                    "status",
                    "invalid.status",
                    "Status must be Active or Inactive"
            );
        }

        if (bindingResult.hasErrors()) {
            return "restaurant/edit-restaurant";
        }

        Restaurant existingRestaurant =
                restaurantService.getRestaurantById(id);

        if (existingRestaurant == null) {
            return "redirect:/restaurant";
        }

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

    @GetMapping("/delete/{id}")
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