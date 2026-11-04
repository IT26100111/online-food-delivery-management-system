
package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.Delivery;
import com.oop.fooddelivery.model.DriverAccount;
import com.oop.fooddelivery.service.DeliveryService;
import com.oop.fooddelivery.service.DriverAccountService;

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
    private final DriverAccountService driverAccountService;

    public DeliveryController(
            DeliveryService deliveryService,
            DriverAccountService driverAccountService) {

        this.deliveryService = deliveryService;
        this.driverAccountService = driverAccountService;
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
                "Pending",
                "Assigned",
                "Out for Delivery",
                "Delivered",
                "Cancelled"
        );
    }

    // Display all deliveries for Admin
    @GetMapping
    public String viewDeliveries(Model model) {

        model.addAttribute(
                "deliveries",
                deliveryService.getAllDeliveries()
        );

        return "delivery/delivery-list";
    }

    // Show assign delivery form
    @GetMapping("/add")
    public String showAddForm(Model model) {

        Delivery delivery = new Delivery();
        delivery.setDeliveryStatus("Pending");

        model.addAttribute("delivery", delivery);
        model.addAttribute(
                "drivers",
                driverAccountService.getAllDrivers()
        );

        return "delivery/assign-delivery";
    }

    // Save new delivery and connect it to the selected driver
    @PostMapping("/add")
    public String addDelivery(
            @Valid @ModelAttribute("delivery") Delivery delivery,
            BindingResult bindingResult,
            @RequestParam(value = "driverId", required = false) Long driverId,
            Model model) {

        System.out.println("\n========== DELIVERY ASSIGNMENT ==========");
        System.out.println("Selected driver ID received: " + driverId);

        DriverAccount selectedDriver = null;

        if (driverId != null) {
            selectedDriver =
                    driverAccountService.getDriverById(driverId);
        }

        System.out.println("Retrieved driver: " +
                (selectedDriver == null
                        ? "NULL"
                        : selectedDriver.getFullName()));

        System.out.println("Retrieved driver database ID: " +
                (selectedDriver == null
                        ? "NULL"
                        : selectedDriver.getDriverId()));

        System.out.println("Driver status received: " +
                (selectedDriver == null
                        ? "NULL"
                        : selectedDriver.getStatus()));

        // Validate selected driver
        if (selectedDriver == null ||
                selectedDriver.getStatus() == null ||
                !"Active".equalsIgnoreCase(
                        selectedDriver.getStatus().trim())) {

            System.out.println("ERROR: Driver is missing or inactive.");

            model.addAttribute(
                    "drivers",
                    driverAccountService.getAllDrivers()
            );

            model.addAttribute(
                    "driverError",
                    "Please select a valid and active driver."
            );

            return "delivery/assign-delivery";
        }

        // Connect delivery to the selected driver
        delivery.setDriver(selectedDriver);

        // Copy driver details
        delivery.setDeliveryPersonName(selectedDriver.getFullName());
        delivery.setPhoneNumber(selectedDriver.getPhoneNumber());

        // Check delivery validation
        System.out.println("Delivery validation errors: " +
                bindingResult.getAllErrors());

        if (bindingResult.hasErrors()) {

            System.out.println("ERROR: Delivery validation failed.");

            model.addAttribute(
                    "drivers",
                    driverAccountService.getAllDrivers()
            );

            model.addAttribute(
                    "driverError",
                    "Please check the delivery details and try again."
            );

            return "delivery/assign-delivery";
        }

        // Ensure this is a new delivery
        delivery.setDeliveryId(null);

        // Save delivery
        try {

            System.out.println("Saving delivery...");

            Delivery savedDelivery =
                    deliveryService.saveDelivery(delivery);

            System.out.println("DELIVERY SAVED SUCCESSFULLY!");
            System.out.println("Saved delivery ID: " +
                    savedDelivery.getDeliveryId());

            System.out.println("Saved delivery driver ID: " +
                    (savedDelivery.getDriver() == null
                            ? "NULL"
                            : savedDelivery.getDriver().getDriverId()));

            System.out.println("========================================\n");

            return "redirect:/delivery";

        } catch (Exception e) {

            System.out.println("ERROR WHILE SAVING DELIVERY:");
            e.printStackTrace();

            model.addAttribute(
                    "drivers",
                    driverAccountService.getAllDrivers()
            );

            model.addAttribute(
                    "driverError",
                    "Could not save the delivery. Please try again."
            );

            return "delivery/assign-delivery";
        }
    }

    // Show edit form
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        Delivery delivery = deliveryService.getDeliveryById(id);

        if (delivery == null) {
            return "redirect:/delivery";
        }

        model.addAttribute("delivery", delivery);

        return "delivery/edit-delivery";
    }

    // Update delivery
    @PostMapping("/edit/{id}")
    public String updateDelivery(
            @PathVariable Long id,
            @Valid @ModelAttribute("delivery") Delivery delivery,
            BindingResult bindingResult,
            Model model) {

        Delivery existingDelivery =
                deliveryService.getDeliveryById(id);

        if (existingDelivery == null) {
            return "redirect:/delivery";
        }

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "deliveryError",
                    "Please check the delivery details."
            );

            delivery.setDeliveryId(id);

            return "delivery/edit-delivery";
        }

        delivery.setDeliveryId(id);
        delivery.setDriver(existingDelivery.getDriver());

        deliveryService.saveDelivery(delivery);

        return "redirect:/delivery";
    }

    // Delete delivery
    @PostMapping("/delete/{id}")
    public String deleteDelivery(@PathVariable Long id) {

        deliveryService.deleteDelivery(id);

        return "redirect:/delivery";
    }

    // Live delivery tracking
    @GetMapping("/tracking/{id}")
    public String showTrackingMap(
            @PathVariable Long id,
            Model model) {

        Delivery delivery = deliveryService.getDeliveryById(id);

        if (delivery == null) {
            return "redirect:/delivery";
        }

        model.addAttribute("delivery", delivery);

        return "delivery/tracking-map";
    }
}
