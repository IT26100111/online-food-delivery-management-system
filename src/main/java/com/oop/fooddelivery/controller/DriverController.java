
package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.service.DeliveryService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/driver")
public class DriverController {

    private final DeliveryService deliveryService;

    public DriverController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // Display deliveries assigned to the logged-in driver
    @GetMapping("/deliveries")
    public String showMyDeliveries(
            Authentication authentication,
            Model model) {

        String driverEmail = authentication.getName();

        model.addAttribute(
                "deliveries",
                deliveryService.getDeliveriesForDriver(driverEmail)
        );

        return "driver/my-deliveries";
    }
}
