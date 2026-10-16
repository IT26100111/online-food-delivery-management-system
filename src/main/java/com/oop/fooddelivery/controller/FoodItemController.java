package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.FoodItem;
import com.oop.fooddelivery.service.FoodItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/food")
public class FoodItemController {

    private final FoodItemService foodItemService;

    public FoodItemController(FoodItemService foodItemService) {
        this.foodItemService = foodItemService;
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
    public String addFoodItem(@ModelAttribute FoodItem foodItem) {
        foodItemService.saveFoodItem(foodItem);
        return "redirect:/food";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {

        FoodItem foodItem = foodItemService.getFoodItemById(id);

        if (foodItem == null) {
            return "redirect:/food";
        }

        model.addAttribute("foodItem", foodItem);
        return "food/edit-food";
    }

    @PostMapping("/edit/{id}")
    public String updateFoodItem(@PathVariable Long id,
                                 @ModelAttribute FoodItem foodItem) {

        foodItem.setFoodItemId(id);
        foodItemService.saveFoodItem(foodItem);

        return "redirect:/food";
    }

    @GetMapping("/delete/{id}")
    public String deleteFoodItem(@PathVariable Long id) {
        foodItemService.deleteFoodItem(id);
        return "redirect:/food";
    }
}
