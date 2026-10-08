package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.Delivery;
import com.oop.fooddelivery.service.DeliveryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // READ - display all deliveries
    @GetMapping
    public String viewDeliveries(Model model) {
        model.addAttribute("deliveries", deliveryService.getAllDeliveries());
        return "delivery/delivery-list";
    }

    // Show assign delivery form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("delivery", new Delivery());
        return "delivery/assign-delivery";
    }

    // CREATE - save new delivery
    @PostMapping("/add")
    public String addDelivery(@ModelAttribute Delivery delivery) {
        deliveryService.saveDelivery(delivery);
        return "redirect:/delivery";
    }

    // Show edit form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {

        Delivery delivery = deliveryService.getDeliveryById(id);

        if (delivery == null) {
            return "redirect:/delivery";
        }

        model.addAttribute("delivery", delivery);
        return "delivery/edit-delivery";
    }

    // UPDATE
    @PostMapping("/edit/{id}")
    public String updateDelivery(@PathVariable Long id,
                                 @ModelAttribute Delivery delivery) {

        delivery.setDeliveryId(id);
        deliveryService.saveDelivery(delivery);

        return "redirect:/delivery";
    }

    // DELETE
    @GetMapping("/delete/{id}")
    public String deleteDelivery(@PathVariable Long id) {

        deliveryService.deleteDelivery(id);

        return "redirect:/delivery";
    }
}