
package com.oop.fooddelivery.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    // Account selection page
    @GetMapping("/login")
    public String showLoginOptions() {
        return "auth/welcome";
    }

    // Admin login page
    @GetMapping("/admin/login")
    public String showAdminLogin() {
        return "auth/admin-login";
    }

    // Driver login page
    @GetMapping("/driver/login")
    public String showDriverLogin() {
        return "auth/driver-login";
    }
}
