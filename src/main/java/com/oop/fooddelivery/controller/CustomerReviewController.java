package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.Order;
import com.oop.fooddelivery.model.OrderStatus;
import com.oop.fooddelivery.service.FeedbackAccessTokenService;
import com.oop.fooddelivery.service.FeedbackService;
import com.oop.fooddelivery.service.OrderService;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Customer-facing guest review flow. A private, unexpired checkout token
 * authorizes access to exactly one Order; the Order ID alone does not.
 */
@Controller
@RequestMapping("/review")
public class CustomerReviewController {

    private final FeedbackAccessTokenService tokenService;
    private final FeedbackService feedbackService;
    private final OrderService orderService;

    public CustomerReviewController(
            FeedbackAccessTokenService tokenService,
            FeedbackService feedbackService,
            OrderService orderService) {
        this.tokenService = tokenService;
        this.feedbackService = feedbackService;
        this.orderService = orderService;
    }

    @GetMapping
    public String showReview(
            @RequestParam(name = "token", required = false) String token,
            Model model,
            HttpServletResponse response) {

        protectPrivatePage(response);
        return prepareReviewPage(token, model, response);
    }

    @PostMapping
    public String submitReview(
            @RequestParam(name = "token", required = false) String token,
            @RequestParam(name = "rating", required = false) String ratingText,
            @RequestParam(name = "reviewMessage", required = false) String reviewMessage,
            Model model,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        protectPrivatePage(response);

        // Re-validate the private token, order status and duplicate rule on POST.
        String page = prepareReviewPage(token, model, response);
        if (!Boolean.TRUE.equals(model.getAttribute("canReview"))) {
            return page;
        }

        Order order = findAuthorizedOrder(token).orElse(null);
        if (order == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("invalidLink", true);
            model.addAttribute("canReview", false);
            return "public/review";
        }

        try {
            Integer rating = ratingText == null
                    ? null
                    : Integer.valueOf(ratingText);

            // FeedbackService enforces DELIVERED, 1-5 stars, message validation,
            // and one review per Order again inside its own transaction.
            feedbackService.createFeedback(
                    order.getOrderId(), rating, reviewMessage);

            redirectAttributes.addFlashAttribute("reviewSubmitted", true);
            // Never include the bearer token in the success URL.
            return "redirect:/review/thank-you";

        } catch (NumberFormatException ex) {
            model.addAttribute("formError", "Please select a rating from 1 to 5.");
        } catch (IllegalArgumentException ex) {
            model.addAttribute("formError", ex.getMessage());
        }

        model.addAttribute("enteredRating", ratingText);
        model.addAttribute("enteredMessage", reviewMessage);
        return "public/review";
    }

    @GetMapping("/thank-you")
    public String thankYou(Model model, HttpServletResponse response) {
        protectPrivatePage(response);
        if (!Boolean.TRUE.equals(model.getAttribute("reviewSubmitted"))) {
            return "redirect:/";
        }
        model.addAttribute("submitted", true);
        return "public/review";
    }

    private String prepareReviewPage(
            String token,
            Model model,
            HttpServletResponse response) {

        Optional<Order> authorizedOrder = findAuthorizedOrder(token);
        if (authorizedOrder.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("invalidLink", true);
            return "public/review";
        }

        Order order = authorizedOrder.get();
        model.addAttribute("orderId", order.getOrderId());
        model.addAttribute("restaurantName", order.getRestaurantName());

        if (order.getStatus() == OrderStatus.CANCELLED) {
            model.addAttribute("cancelled", true);
            return "public/review";
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            model.addAttribute("pending", true);
            return "public/review";
        }

        if (!feedbackService.getFeedbackByOrderId(order.getOrderId()).isEmpty()) {
            model.addAttribute("alreadyReviewed", true);
            return "public/review";
        }

        model.addAttribute("token", token);
        model.addAttribute("canReview", true);
        return "public/review";
    }

    private Optional<Order> findAuthorizedOrder(String token) {
        // Reload the Order inside OrderService: avoids relying on a detached
        // lazy relation when the token service transaction has ended.
        return tokenService.findOrderByToken(token)
                .map(order -> orderService.getOrderById(order.getOrderId()));
    }

    private void protectPrivatePage(HttpServletResponse response) {
        response.setHeader("Cache-Control", "private, no-store, max-age=0");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("X-Robots-Tag", "noindex, nofollow, noarchive");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; style-src 'self' 'unsafe-inline'; "
                        + "img-src 'self' data:; script-src 'none'; "
                        + "base-uri 'none'; form-action 'self'; frame-ancestors 'none'");
    }
}
