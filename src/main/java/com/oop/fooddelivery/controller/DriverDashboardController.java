package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.DriverAccount;
import com.oop.fooddelivery.model.Delivery;
import com.oop.fooddelivery.service.DriverAccountService;
import com.oop.fooddelivery.service.DeliveryService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/driver")
public class DriverDashboardController {

    private final DriverAccountService driverAccountService;
    private final DeliveryService deliveryService;

    public DriverDashboardController(
            DriverAccountService driverAccountService,
            DeliveryService deliveryService) {

        this.driverAccountService = driverAccountService;
        this.deliveryService = deliveryService;
    }

    @GetMapping("/dashboard")
    public String driverDashboard(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        DriverAccount driver = driverAccountService
                .getDriverByEmail(email)
                .orElse(null);

        List<Delivery> deliveries =
                deliveryService.getDeliveriesForDriver(email);

        long totalDeliveries = deliveries.size();

        long inProgress = deliveries.stream()
                .filter(d ->
                        "Assigned".equalsIgnoreCase(d.getDeliveryStatus())
                                || "Out for Delivery".equalsIgnoreCase(d.getDeliveryStatus())
                                || "In Progress".equalsIgnoreCase(d.getDeliveryStatus()))
                .count();

        long completed = deliveries.stream()
                .filter(d ->
                        "Delivered".equalsIgnoreCase(d.getDeliveryStatus()))
                .count();

        model.addAttribute("driver", driver);
        model.addAttribute("deliveries", deliveries);
        model.addAttribute("totalDeliveries", totalDeliveries);
        model.addAttribute("inProgress", inProgress);
        model.addAttribute("completed", completed);

        return "driver/driver-dashboard";
    }

    @GetMapping("/profile")
    public String driverProfile(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        DriverAccount driver = driverAccountService
                .getDriverByEmail(email)
                .orElse(null);

        model.addAttribute("driver", driver);

        return "driver/driver-profile";
    }
}