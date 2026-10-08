package com.oop.fooddelivery.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FoodItemController {

    @GetMapping("/food")
    public String foodList() {
        return "food/food-list";
    }

    @GetMapping("/food/add")
    public String addFood() {
        return "food/add-food";
    }

    @GetMapping("/food/edit")
    public String editFood() {
        return "food/edit-food";
    }
}