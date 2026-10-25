package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.*;
import com.oop.fooddelivery.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.oop.fooddelivery.service.CustomerService;
import com.oop.fooddelivery.service.FoodItemService;
import com.oop.fooddelivery.model.OrderStatus;


@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final CustomerService customerService;

    private final FoodItemService foodItemService;


    public OrderController(OrderService orderService,CustomerService customerService,FoodItemService foodItemService) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.foodItemService = foodItemService;
    }


    @GetMapping
    public String viewOrders(Model model) {

        model.addAttribute(
                "orders",
                orderService.getAllOrders()
        );

        return "order/order-list";
    }


    @GetMapping("/add")
    public String showAddForm(Model model) {

        model.addAttribute(
                "order",
                new Order()
        );

        model.addAttribute(
                "customers",
                customerService.getAllCustomers()
        );

        return "order/add-order";
    }


    @PostMapping("/add")
    public String addOrder(@ModelAttribute Order order,@RequestParam Long customerId) {

        Customer customer = customerService.getCustomerById(customerId);

        if(customer == null){
            return "redirect:/order/add";
        }
        order.setCustomer(customer);
        order.calculateTotalAmount();
        orderService.saveOrder(order);

        return "redirect:/order";
    }


    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               Model model) {

        Order order =
                orderService.getOrderById(id);

        if (order == null) {
            return "redirect:/order";
        }

        model.addAttribute(
                "order",
                order
        );

        model.addAttribute(
                "customers",
                customerService.getAllCustomers()
        );

        return "order/edit-order";
    }


    @PostMapping("/edit/{id}")
    public String updateOrder(
            @PathVariable Long id,
            @ModelAttribute Order formOrder,
            @RequestParam Long customerId) {

        Customer customer =
                customerService.getCustomerById(customerId);

        if (customer == null) {
            return "redirect:/order/edit/" + id;
        }

        Order existingOrder = orderService.getOrderById(id);
        if(existingOrder == null){
            return "redirect:/order";
        }

        existingOrder.setCustomer(customer);
        existingOrder.setRestaurantName(formOrder.getRestaurantName());
        existingOrder.setOrderDate(formOrder.getOrderDate());

        OrderStatus requestedStatus = formOrder.getStatus();

        if (!existingOrder.changeStatus(requestedStatus)) {
            return "redirect:/order/edit/" + id
                    + "?statusError=true";
        }
        existingOrder.setDeliveryAddress(formOrder.getDeliveryAddress());

        existingOrder.calculateTotalAmount();

        orderService.saveOrder(existingOrder);

        return "redirect:/order";
    }


    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id) {

        orderService.deleteOrder(id);

        return "redirect:/order";
    }

    @GetMapping("/{id}/items/add")
    public String showAddItemForm(@PathVariable Long id, Model model){
        Order order = orderService.getOrderById(id);
        if(order == null){
            return "redirect:/order";
        }
        model.addAttribute("order",order);
        model.addAttribute("orderItem", new OrderItem());
        model.addAttribute("foodItems",foodItemService.getAllFoodItems());

        return "order/add-order-item";
    }

    @PostMapping("/{id}/items/add")
    public String addItemToOrder(@PathVariable Long id,
                                 @ModelAttribute OrderItem orderItem,
                                 @RequestParam Long foodItemId) {

        Order order = orderService.getOrderById(id);

        if (order == null) {
            return "redirect:/order";
        }
        FoodItem foodItem = foodItemService.getFoodItemById(foodItemId);
        if(foodItem == null){
            return "redirect:/order/" + id + "/items/add";
        }

        if(orderItem.getQuantity() == null || orderItem.getQuantity() < 1){
            return "redirect:/order/" + id + "/items/add?quantityError=true";
        }

        if(!"Available".equalsIgnoreCase(foodItem.getAvailability())){
            return "redirect:/order/" + id + "/items/add?unavailable=true";
        }

        orderItem.setFoodItem(foodItem);
        order.addOrderItem(orderItem);

        orderService.saveOrder(order);

        return "redirect:/order" + id;
    }

    @GetMapping("/{id}")
    public String viewOrderDetails(@PathVariable Long id,
                                   Model model) {

        Order order = orderService.getOrderById(id);

        if (order == null) {
            return "redirect:/order";
        }

        model.addAttribute("order", order);

        return "order/order-details";
    }

    @GetMapping("/{orderId}/items/delete/{itemId}")
    public String deleteOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId) {

        orderService.removeOrderItem(
                orderId,
                itemId
        );

        return "redirect:/order/" + orderId;
    }
}