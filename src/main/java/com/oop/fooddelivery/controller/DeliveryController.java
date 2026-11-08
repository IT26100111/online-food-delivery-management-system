package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.Delivery;
import com.oop.fooddelivery.model.Order;
import com.oop.fooddelivery.model.OrderStatus;
import com.oop.fooddelivery.service.DeliveryService;
import com.oop.fooddelivery.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final OrderService orderService;

    public DeliveryController(DeliveryService deliveryService,
                              OrderService orderService) {
        this.deliveryService = deliveryService;
        this.orderService = orderService;
    }

    @InitBinder("delivery")
    public void bindDeliveryFields(WebDataBinder binder) {
        binder.setAllowedFields(
                "orderId",
                "deliveryPersonName",
                "phoneNumber",
                "deliveryAddress",
                "deliveryDate",
                "deliveryStatus"
        );
    }

    @ModelAttribute("deliveryStatuses")
    public List<String> deliveryStatuses() {
        return List.of(
                "Pending", "Assigned", "Out for Delivery", "Delivered", "Cancelled"
        );
    }

    @GetMapping
    public String viewDeliveries(Model model) {
        model.addAttribute("deliveries", deliveryService.getAllDeliveries());
        return "delivery/delivery-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        Delivery delivery = new Delivery();
        delivery.setDeliveryStatus("Pending");
        model.addAttribute("delivery", delivery);
        populateAssignableOrders(model);
        return "delivery/assign-delivery";
    }

    @PostMapping("/add")
    public String addDelivery(@Valid @ModelAttribute("delivery") Delivery delivery,
                              BindingResult bindingResult,
                              Model model) {
        if (bindingResult.hasErrors()) {
            populateAssignableOrders(model);
            return "delivery/assign-delivery";
        }

        try {
            // Never accept a client-supplied delivery ID on creation.
            delivery.setDeliveryId(null);
            deliveryService.saveDelivery(delivery);
            return "redirect:/delivery";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            bindingResult.reject("delivery.assignment", ex.getMessage());
            populateAssignableOrders(model);
            return "delivery/assign-delivery";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Delivery delivery = deliveryService.getDeliveryById(id);
        if (delivery == null) {
            return "redirect:/delivery";
        }
        model.addAttribute("delivery", delivery);
        populateAssignedOrder(model, delivery.getOrderId());
        return "delivery/edit-delivery";
    }

    @PostMapping("/edit/{id}")
    public String updateDelivery(@PathVariable Long id,
                                 @Valid @ModelAttribute("delivery") Delivery delivery,
                                 BindingResult bindingResult,
                                 Model model) {
        Delivery existing = deliveryService.getDeliveryById(id);
        if (existing == null) {
            return "redirect:/delivery";
        }

        // Order association and delivery ID come from the saved record,
        // never from editable browser fields.
        delivery.setDeliveryId(id);
        delivery.setOrderId(existing.getOrderId());
        populateAssignedOrder(model, existing.getOrderId());

        if (bindingResult.hasErrors()) {
            return "delivery/edit-delivery";
        }

        try {
            deliveryService.saveDelivery(delivery);
            return "redirect:/delivery";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            bindingResult.reject("delivery.update", ex.getMessage());
            return "delivery/edit-delivery";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteDelivery(@PathVariable Long id) {
        deliveryService.deleteDelivery(id);
        return "redirect:/delivery";
    }

    private void populateAssignableOrders(Model model) {
        List<Order> orders = orderService.getAllOrders().stream()
                .filter(order -> order.getOrderId() != null)
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .filter(order -> order.getStatus() != OrderStatus.DELIVERED)
                .filter(order -> deliveryService.getDeliveryByOrderId(
                        order.getOrderId()) == null)
                .sorted(Comparator.comparing(Order::getOrderId).reversed())
                .toList();
        model.addAttribute("eligibleOrders", orders);
    }

    private void populateAssignedOrder(Model model, Long orderId) {
        model.addAttribute("assignedOrder", orderService.getOrderById(orderId));
    }
}
