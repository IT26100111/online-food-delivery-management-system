package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.*;
import com.oop.fooddelivery.service.CustomerService;
import com.oop.fooddelivery.service.FoodItemService;
import com.oop.fooddelivery.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final FoodItemService foodItemService;

    public OrderController(OrderService orderService, CustomerService customerService,
                           FoodItemService foodItemService) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.foodItemService = foodItemService;
    }

    // Do not bind orderId, customer, totalAmount or orderItems from an HTML form.
    @InitBinder("order")
    public void bindOnlyEditableOrderFields(WebDataBinder binder) {
        binder.setAllowedFields("restaurantName", "orderDate", "status", "deliveryAddress");
    }

    @GetMapping
    public String viewOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "order/order-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        Order order = new Order();
        order.setOrderDate(LocalDate.now());
        order.setStatus(OrderStatus.PENDING);
        model.addAttribute("order", order);
        model.addAttribute("customers", customerService.getAllCustomers());
        return "order/add-order";
    }

    @PostMapping("/add")
    public String addOrder(
            @Valid @ModelAttribute("order") Order formOrder,
            BindingResult bindingResult,
            @RequestParam(required = false) Long customerId,
            Model model,
            RedirectAttributes redirectAttributes) {

        Customer customer = customerService.getCustomerById(customerId);
        if (customer == null) {
            bindingResult.reject("customer.required", "Please select an existing customer.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("customers", customerService.getAllCustomers());
            model.addAttribute("selectedCustomerId", customerId);
            return "order/add-order";
        }
        try {
            Order saved = orderService.createAdminOrder(customer, formOrder);
            redirectAttributes.addFlashAttribute("success",
                    "Draft order #" + saved.getOrderId() + " created. Add items next.");
            return "redirect:/order/" + saved.getOrderId();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            bindingResult.reject("order.invalid", ex.getMessage());
            model.addAttribute("customers", customerService.getAllCustomers());
            model.addAttribute("selectedCustomerId", customerId);
            return "order/add-order";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        if (order == null) return "redirect:/order";
        model.addAttribute("order", order);
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("selectedCustomerId",
                order.getCustomer() == null ? null : order.getCustomer().getCustomerId());
        return "order/edit-order";
    }

    @PostMapping("/edit/{id}")
    public String updateOrder(
            @PathVariable Long id,
            @Valid @ModelAttribute("order") Order formOrder,
            BindingResult bindingResult,
            @RequestParam(required = false) Long customerId,
            Model model,
            RedirectAttributes redirectAttributes) {

        Order existing = orderService.getOrderById(id);
        if (existing == null) return "redirect:/order";
        formOrder.setOrderId(id); // For the form action on validation errors.
        Customer customer = customerService.getCustomerById(customerId);
        if (customer == null) {
            bindingResult.reject("customer.required", "Please select an existing customer.");
        }
        if (bindingResult.hasErrors()) {
            prepareEditForm(model, customerId);
            return "order/edit-order";
        }
        try {
            orderService.updateAdminOrder(id, customer, formOrder);
            redirectAttributes.addFlashAttribute("success", "Order updated successfully.");
            return "redirect:/order/" + id;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            bindingResult.reject("order.invalid", ex.getMessage());
            prepareEditForm(model, customerId);
            return "order/edit-order";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.deleteOrder(id);
            redirectAttributes.addFlashAttribute("success", "Draft order deleted.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/order";
    }

    @GetMapping("/{id}/items/add")
    public String showAddItemForm(@PathVariable Long id, Model model,
                                  RedirectAttributes redirectAttributes) {
        Order order = orderService.getOrderById(id);
        if (order == null) return "redirect:/order";
        if (!orderService.canEditOrderItems(order)) {
            redirectAttributes.addFlashAttribute("error",
                    "Items cannot be modified after checkout or preparation.");
            return "redirect:/order/" + id;
        }
        model.addAttribute("order", order);
        model.addAttribute("orderItem", new OrderItem());
        model.addAttribute("foodItems", availableFoodFor(order));
        return "order/add-order-item";
    }

    @PostMapping("/{id}/items/add")
    public String addItemToOrder(
            @PathVariable Long id,
            @RequestParam(required = false) Long foodItemId,
            @RequestParam(required = false) Integer quantity,
            RedirectAttributes redirectAttributes) {
        try {
            FoodItem foodItem = foodItemService.getFoodItemById(foodItemId);
            orderService.addOrderItem(id, foodItem, quantity);
            redirectAttributes.addFlashAttribute("success", "Order item added.");
            return "redirect:/order/" + id;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/order/" + id;
        }
    }

    @GetMapping("/{id}")
    public String viewOrderDetails(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        if (order == null) return "redirect:/order";
        model.addAttribute("order", order);
        model.addAttribute("canEditItems", orderService.canEditOrderItems(order));
        return "order/order-details";
    }

    @PostMapping("/{orderId}/items/delete/{itemId}")
    public String deleteOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            RedirectAttributes redirectAttributes) {
        try {
            orderService.removeOrderItem(orderId, itemId);
            redirectAttributes.addFlashAttribute("success", "Order item removed.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/order/" + orderId;
    }

    private void prepareEditForm(Model model, Long selectedCustomerId) {
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("selectedCustomerId", selectedCustomerId);
    }

    private List<FoodItem> availableFoodFor(Order order) {
        return foodItemService.getAllFoodItems().stream()
                .filter(food -> food.getRestaurant() != null
                        && food.getRestaurant().isActive()
                        && "Available".equals(food.getAvailability())
                        && food.getPrice() != null && food.getPrice() > 0
                        && order.getRestaurantName() != null
                        && order.getRestaurantName().trim().equalsIgnoreCase(
                        food.getRestaurant().getRestaurantName().trim()))
                .toList();
    }
}
