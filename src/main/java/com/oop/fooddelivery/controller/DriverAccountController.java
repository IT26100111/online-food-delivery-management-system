
package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.DriverAccount;
import com.oop.fooddelivery.service.DriverAccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/drivers")
public class DriverAccountController {

    private final DriverAccountService driverAccountService;

    public DriverAccountController(DriverAccountService driverAccountService) {
        this.driverAccountService = driverAccountService;
    }

    // Display all drivers
    @GetMapping
    public String listDrivers(Model model) {
        model.addAttribute("drivers", driverAccountService.getAllDrivers());
        return "driver/driver-list";
    }

    // Show registration form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("driver", new DriverAccount());
        return "driver/add-driver";
    }

    // Save new driver
    @PostMapping("/save")
    public String saveDriver(@ModelAttribute("driver") DriverAccount driver) {
        driverAccountService.saveDriver(driver);
        return "redirect:/admin/drivers";
    }

    // Show edit form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {

        DriverAccount driver = driverAccountService.getDriverById(id);

        if (driver == null) {
            return "redirect:/admin/drivers";
        }

        model.addAttribute("driver", driver);
        return "driver/edit-driver";
    }

    // Update driver
    @PostMapping("/update/{id}")
    public String updateDriver(
            @PathVariable Long id,
            @ModelAttribute("driver") DriverAccount driver) {

        driverAccountService.updateDriver(id, driver);
        return "redirect:/admin/drivers";
    }

    // Delete driver
    @PostMapping("/delete/{id}")
    public String deleteDriver(@PathVariable Long id) {
        driverAccountService.deleteDriver(id);
        return "redirect:/admin/drivers";
    }
}
