
package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.AdminAccount;
import com.oop.fooddelivery.service.AdminAccountService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/account")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    public AdminAccountController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    // Display Admin registration page
    @GetMapping("/register")
    public String showRegistrationPage() {

        if (adminAccountService.adminExists()) {
            return "redirect:/login";
        }

        return "auth/admin-register";
    }

    // Save first Admin account
    @PostMapping("/register")
    public String registerAdmin(@ModelAttribute AdminAccount admin) {

        if (adminAccountService.adminExists()) {
            return "redirect:/login";
        }

        adminAccountService.saveAdmin(admin);

        return "redirect:/login";
    }
}
