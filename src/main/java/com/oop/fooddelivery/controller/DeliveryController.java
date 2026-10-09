package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.Delivery;
import com.oop.fooddelivery.service.DeliveryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @InitBinder("delivery")
    public void bindDeliveryFields(WebDataBinder binder) {
        binder.setAllowedFields("orderId", "deliveryPersonName", "phoneNumber",
                "deliveryAddress", "deliveryDate", "deliveryStatus");
    }

    @ModelAttribute("deliveryStatuses")
    public List<String> deliveryStatuses() {
        return List.of("Pending", "Assigned", "Out for Delivery", "Delivered", "Cancelled");
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
        Delivery delivery = new Delivery();
        delivery.setDeliveryStatus("Pending");
        model.addAttribute("delivery", delivery);
        return "delivery/assign-delivery";
    }

    // CREATE - save new delivery
    @PostMapping("/add")
    public String addDelivery(@Valid @ModelAttribute("delivery") Delivery delivery,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "delivery/assign-delivery";
        }
        delivery.setDeliveryId(null);
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
                                 @Valid @ModelAttribute("delivery") Delivery delivery,
                                 BindingResult bindingResult) {
        if (deliveryService.getDeliveryById(id) == null) {
            return "redirect:/delivery";
        }
        delivery.setDeliveryId(id);
        if (bindingResult.hasErrors()) {
            return "delivery/edit-delivery";
        }
        deliveryService.saveDelivery(delivery);

        return "redirect:/delivery";
    }

    // DELETE
    @PostMapping("/delete/{id}")
    public String deleteDelivery(@PathVariable Long id) {

        deliveryService.deleteDelivery(id);

        return "redirect:/delivery";
    }
}