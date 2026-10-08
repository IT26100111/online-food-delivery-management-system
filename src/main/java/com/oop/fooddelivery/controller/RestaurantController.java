package com.oop.fooddelivery.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RestaurantController {

    @GetMapping("/restaurant")
    public String restaurantList() {
        return "restaurant/restaurant-list";
    }

    @GetMapping("/restaurant/add")
    public String addRestaurant() {
        return "restaurant/add-restaurant";
    }

    @GetMapping("/restaurant/edit")
    public String editRestaurant() {
        return "restaurant/edit-restaurant";
    }
}