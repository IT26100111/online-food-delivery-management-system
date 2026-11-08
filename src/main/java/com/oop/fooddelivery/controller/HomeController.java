
package com.oop.fooddelivery.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // Check whether the visitor is logged in.
        boolean loggedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        // Check whether the logged-in user is an admin.
        boolean admin = loggedIn
                && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority())
                );

        // Send login information to the Thymeleaf page.
        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("admin", admin);

        return "home/index";
    }
}
