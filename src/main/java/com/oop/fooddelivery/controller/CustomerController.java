package com.oop.fooddelivery.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomerController {

    @GetMapping("/customer")
    public String customerList() {
        return "customer/customer-list";
    }

    @GetMapping("/customer/add")
    public String addCustomer() {
        return "customer/add-customer";
    }

    @GetMapping("/customer/edit")
    public String editCustomer() {
        return "customer/edit-customer";
    }
}