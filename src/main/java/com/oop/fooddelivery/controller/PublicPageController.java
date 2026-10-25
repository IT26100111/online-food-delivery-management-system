package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.Order;
import com.oop.fooddelivery.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PublicPageController {

    private final OrderService orderService;
    public PublicPageController(OrderService orderService){
        this.orderService = orderService;
    }

    @GetMapping("/track-order")
    public String trackOrderPage(){
        return "public/track-order";
    }

    @GetMapping("/track-order/search")
    public String searchOrder(
            @RequestParam Long orderId,
            Model model){
        Order order = orderService.getOrderById(orderId);
        if(order == null){
            model.addAttribute("notFound",true);
            return "public/track-order";
        }
        model.addAttribute("order",order);
        return "public/track-order";
    }

    @GetMapping("/restaurants")
    public String restaurants() {
        return "public/restaurants";
    }

    @GetMapping("/menu")
    public String menu() {
        return "public/menu";
    }

    @GetMapping("/recommendations")
    public String recommendations() {
        return "public/recommendations";
    }


    @GetMapping("/about")
    public String about() {
        return "public/about";
    }
}