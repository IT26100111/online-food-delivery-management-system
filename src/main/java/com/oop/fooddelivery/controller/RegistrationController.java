
package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.auth.RegistrationForm;
import com.oop.fooddelivery.service.AppUserService;

import jakarta.validation.Valid;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {

    private final AppUserService appUserService;

    public RegistrationController(
            AppUserService appUserService) {

        this.appUserService = appUserService;
    }

    // Display customer registration page.
    @GetMapping("/register")
    public String showRegistration(Model model) {

        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute(
                    "registrationForm",
                    new RegistrationForm()
            );
        }

        return "public/register";
    }

    // Handle customer registration.
    @PostMapping("/register")
    public String registerCustomer(
            @Valid @ModelAttribute("registrationForm")
            RegistrationForm registrationForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        // Confirm that both passwords match.
        if (registrationForm.getPassword() != null
                && registrationForm.getConfirmPassword() != null
                && !registrationForm.getPassword().equals(
                registrationForm.getConfirmPassword())) {

            bindingResult.rejectValue(
                    "confirmPassword",
                    "password.mismatch",
                    "Passwords do not match."
            );
        }

        if (bindingResult.hasErrors()) {
            return "public/register";
        }

        try {

            // The service validates inputs,
            // encrypts the password and assigns CUSTOMER.
            appUserService.registerCustomer(
                    registrationForm.getEmail(),
                    registrationForm.getPassword()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Your account was created successfully!"
            );

            return "redirect:/register";

        } catch (IllegalArgumentException ex) {

            bindingResult.reject(
                    "registration.error",
                    ex.getMessage()
            );

            return "public/register";

        } catch (DataIntegrityViolationException ex) {

            // Handles potential duplicate email
            // conflicts at the database level.
            bindingResult.reject(
                    "registration.error",
                    "Registration could not be completed. " +
                            "Please check your details and try again."
            );

            return "public/register";
        }
    }
}
