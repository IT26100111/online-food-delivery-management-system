package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.Payment;
import com.oop.fooddelivery.model.PaymentMethod;
import com.oop.fooddelivery.model.PaymentStatus;
import com.oop.fooddelivery.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payment")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public String viewPayments(Model model) {
        model.addAttribute("payments", paymentService.getAllPayments());
        return "payment/payment-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        populateAddForm(model);
        return "payment/add-payment";
    }

    @PostMapping("/add")
    public String addPayment(@RequestParam(required = false) Long orderId,
                             @RequestParam(required = false) String paymentMethod,
                             Model model) {
        try {
            PaymentMethod method = PaymentMethod.valueOf(paymentMethod == null ? "" : paymentMethod);
            paymentService.createPayment(orderId, method);
            return "redirect:/payment";
        } catch (IllegalArgumentException ex) {
            populateAddForm(model);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("selectedOrderId", orderId);
            model.addAttribute("selectedMethod", paymentMethod);
            return "payment/add-payment";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Payment payment = paymentService.getPaymentById(id);
        if (payment == null) return "redirect:/payment";
        model.addAttribute("payment", payment);
        return "payment/edit-payment";
    }

    @PostMapping("/edit/{id}")
    public String updatePayment(@PathVariable Long id,
                                @RequestParam(required = false) String paymentStatus,
                                Model model) {
        try {
            PaymentStatus requested = PaymentStatus.valueOf(paymentStatus == null ? "" : paymentStatus);
            paymentService.changePaymentStatus(id, requested);
            return "redirect:/payment";
        } catch (IllegalArgumentException ex) {
            Payment payment = paymentService.getPaymentById(id);
            if (payment == null) return "redirect:/payment";
            model.addAttribute("payment", payment);
            model.addAttribute("error", ex.getMessage());
            return "payment/edit-payment";
        }
    }

    @PostMapping("/delete/{id}")
    public String deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return "redirect:/payment";
    }

    private void populateAddForm(Model model) {
        model.addAttribute("orders", paymentService.getOrders());
        model.addAttribute("methods", PaymentMethod.values());
    }
}
