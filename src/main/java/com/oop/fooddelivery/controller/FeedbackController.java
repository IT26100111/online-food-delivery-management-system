package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.Feedback;
import com.oop.fooddelivery.model.Order;
import com.oop.fooddelivery.model.OrderStatus;
import com.oop.fooddelivery.service.FeedbackService;
import com.oop.fooddelivery.service.OrderService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final OrderService orderService;

    public FeedbackController(
            FeedbackService feedbackService,
            OrderService orderService) {

        this.feedbackService = feedbackService;
        this.orderService = orderService;
    }

    // READ: Display all feedback
    @GetMapping
    public String viewFeedback(Model model) {

        model.addAttribute(
                "feedbackList",
                feedbackService.getAllFeedback()
        );

        return "feedback/feedback-list";
    }

    // CREATE: Display add feedback form
    @GetMapping("/add")
    public String showAddForm(Model model) {

        List<Order> deliveredOrders = orderService
                .getAllOrders()
                .stream()
                .filter(order ->
                        order.getStatus() == OrderStatus.DELIVERED
                )
                .toList();

        model.addAttribute("orders", deliveredOrders);

        return "feedback/add-feedback";
    }

    // CREATE: Save new feedback
    @PostMapping("/add")
    public String addFeedback(
            @RequestParam Long orderId,
            @RequestParam Integer rating,
            @RequestParam String reviewMessage,
            RedirectAttributes redirectAttributes) {

        try {
            feedbackService.createFeedback(
                    orderId,
                    rating,
                    reviewMessage
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Feedback submitted successfully."
            );

            return "redirect:/feedback";

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );

            return "redirect:/feedback/add";
        }
    }

    // UPDATE: Display edit form
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Feedback feedback =
                feedbackService.getFeedbackById(id);

        if (feedback == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Feedback not found."
            );

            return "redirect:/feedback";
        }

        model.addAttribute("feedback", feedback);

        return "feedback/edit-feedback";
    }

    // UPDATE: Save rating and review changes
    @PostMapping("/edit/{id}")
    public String updateFeedback(
            @PathVariable Long id,
            @RequestParam Integer rating,
            @RequestParam String reviewMessage,
            RedirectAttributes redirectAttributes) {

        try {

            feedbackService.updateFeedback(
                    id,
                    rating,
                    reviewMessage
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Feedback updated successfully."
            );

            return "redirect:/feedback";

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );

            return "redirect:/feedback/edit/" + id;
        }
    }

    // UPDATE: Change feedback visibility
    @PostMapping("/status/{id}")
    public String changeStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {

        try {

            feedbackService.updateFeedbackStatus(
                    id,
                    status
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Feedback status updated."
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/feedback";
    }

    // DELETE: Remove feedback
    @PostMapping("/delete/{id}")
    public String deleteFeedback(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            feedbackService.deleteFeedback(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Feedback deleted successfully."
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/feedback";
    }
}