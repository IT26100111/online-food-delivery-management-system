package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.Customer;
import com.oop.fooddelivery.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // Protect server-owned ID against mass assignment from browser requests.
    @InitBinder("customer")
    public void restrictCustomerFields(WebDataBinder binder) {
        binder.setAllowedFields(
                "fullName", "email", "phoneNumber", "address", "status"
        );
    }

    // READ: list all customers or search by name/email.
    @GetMapping
    public String viewCustomers(
            @RequestParam(name = "q", required = false) String query,
            Model model) {
        model.addAttribute("customers", customerService.searchCustomers(query));
        model.addAttribute("q", query == null ? "" : query);
        return "customer/customer-list";
    }

    // CREATE: open form.
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/add-customer";
    }

    // CREATE: validate and save.
    @PostMapping("/add")
    public String addCustomer(
            @Valid @ModelAttribute("customer") Customer customer,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "customer/add-customer";
        }
        customer.setCustomerId(null);
        customerService.saveCustomer(customer);
        redirectAttributes.addFlashAttribute("success", "Customer added successfully.");
        return "redirect:/customer";
    }

    // UPDATE: load the existing customer into the form.
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomerById(id);
        if (customer == null) {
            return "redirect:/customer";
        }
        model.addAttribute("customer", customer);
        model.addAttribute("customerId", id);
        return "customer/edit-customer";
    }

    // UPDATE: validate, then update the existing stored entity.
    @PostMapping("/edit/{id}")
    public String updateCustomer(
            @PathVariable Long id,
            @Valid @ModelAttribute("customer") Customer customer,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (customerService.getCustomerById(id) == null) {
            return "redirect:/customer";
        }
        model.addAttribute("customerId", id);
        if (bindingResult.hasErrors()) {
            return "customer/edit-customer";
        }
        customerService.updateCustomer(id, customer);
        redirectAttributes.addFlashAttribute("success", "Customer updated successfully.");
        return "redirect:/customer";
    }

    // DELETE: POST + CSRF; refuse deletion when orders exist.
    @PostMapping("/delete/{id}")
    public String deleteCustomer(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("success", "Customer deleted successfully.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/customer";
    }
}
