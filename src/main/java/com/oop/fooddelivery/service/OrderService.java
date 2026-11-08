package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.*;
import com.oop.fooddelivery.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final DeliveryRepository deliveryRepository;
    private final FeedbackRepository feedbackRepository;
    private final FeedbackAccessTokenRepository reviewTokenRepository;
    private final OrderTrackingTokenRepository trackingTokenRepository;

    public OrderService(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            DeliveryRepository deliveryRepository,
            FeedbackRepository feedbackRepository,
            FeedbackAccessTokenRepository reviewTokenRepository,
            OrderTrackingTokenRepository trackingTokenRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.paymentRepository = Objects.requireNonNull(paymentRepository);
        this.deliveryRepository = Objects.requireNonNull(deliveryRepository);
        this.feedbackRepository = Objects.requireNonNull(feedbackRepository);
        this.reviewTokenRepository = Objects.requireNonNull(reviewTokenRepository);
        this.trackingTokenRepository = Objects.requireNonNull(trackingTokenRepository);
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return id == null ? null : orderRepository.findById(id).orElse(null);
    }

    // Shared with checkout and delivery: retain this signature and behavior.
    @Transactional
    public Order saveOrder(Order order) {
        if (order == null) throw new IllegalArgumentException("Order is required.");
        return orderRepository.save(order);
    }

    // Administrative creation is separate from the guest checkout workflow.
    @Transactional
    public Order createAdminOrder(Customer customer, Order form) {
        if (customer == null || customer.getCustomerId() == null) {
            throw new IllegalArgumentException("Select an existing customer.");
        }
        validateDetails(form);
        if (form.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("New orders must start as Pending.");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setRestaurantName(form.getRestaurantName().trim());
        order.setOrderDate(form.getOrderDate());
        order.setDeliveryAddress(form.getDeliveryAddress().trim());
        order.setStatus(OrderStatus.PENDING);
        order.calculateTotalAmount(); // Manual draft initially has no items.
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateAdminOrder(Long id, Customer customer, Order form) {
        if (customer == null || customer.getCustomerId() == null) {
            throw new IllegalArgumentException("Select an existing customer.");
        }
        validateDetails(form);
        Order existing = requireOrder(id);
        OrderStatus current = existing.getStatus();
        OrderStatus requested = form.getStatus();

        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED
                || current == OrderStatus.OUT_FOR_DELIVERY) {
            throw new IllegalStateException(
                    "This order is already dispatched or closed and cannot be edited here.");
        }
        if (requested == OrderStatus.OUT_FOR_DELIVERY || requested == OrderStatus.DELIVERED) {
            throw new IllegalStateException(
                    "Dispatch and delivery completion must be managed through Delivery Management.");
        }

        // Metadata is locked as soon as payment exists or preparation starts.
        boolean detailsChanged = !Objects.equals(customer.getCustomerId(),
                        existing.getCustomer() == null ? null : existing.getCustomer().getCustomerId())
                || !Objects.equals(form.getRestaurantName().trim(), existing.getRestaurantName())
                || !Objects.equals(form.getOrderDate(), existing.getOrderDate())
                || !Objects.equals(form.getDeliveryAddress().trim(), existing.getDeliveryAddress());

        if (detailsChanged && (current != OrderStatus.PENDING
                || paymentRepository.existsByOrder_OrderId(id)
                || deliveryRepository.existsByOrderId(id))) {
            throw new IllegalStateException(
                    "Order details are locked after payment, assignment or preparation starts.");
        }

        if (current == OrderStatus.PENDING && requested == OrderStatus.PREPARING
                && (existing.getOrderItems().isEmpty() || existing.getTotalAmount() == null
                || existing.getTotalAmount() <= 0)) {
            throw new IllegalStateException(
                    "Add at least one food item before starting preparation.");
        }
        if (requested == OrderStatus.CANCELLED) {
            if (deliveryRepository.existsByOrderId(id)) {
                throw new IllegalStateException(
                        "Cancel the assigned delivery through Delivery Management first.");
            }
            if (paymentRepository.existsByOrder_OrderIdAndPaymentStatus(id, PaymentStatus.PAID)) {
                throw new IllegalStateException(
                        "A paid order must be handled through the refund process before cancellation.");
            }
        }
        if (!existing.changeStatus(requested)) {
            throw new IllegalArgumentException("Invalid order status transition.");
        }
        if (detailsChanged) {
            existing.setCustomer(customer);
            existing.setRestaurantName(form.getRestaurantName().trim());
            existing.setOrderDate(form.getOrderDate());
            existing.setDeliveryAddress(form.getDeliveryAddress().trim());
        }
        existing.calculateTotalAmount();
        return orderRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public boolean canEditOrderItems(Order order) {
        if (order == null || order.getOrderId() == null
                || order.getStatus() != OrderStatus.PENDING) return false;
        Long id = order.getOrderId();
        return !paymentRepository.existsByOrder_OrderId(id)
                && !deliveryRepository.existsByOrderId(id);
    }

    @Transactional
    public void addOrderItem(Long orderId, FoodItem food, Integer quantity) {
        Order order = requireOrder(orderId);
        requireEditableItems(order);
        if (quantity == null || quantity < 1 || quantity > 20) {
            throw new IllegalArgumentException("Quantity must be between 1 and 20.");
        }
        if (food == null || food.getFoodItemId() == null || food.getRestaurant() == null
                || !food.getRestaurant().isActive() || !"Available".equals(food.getAvailability())
                || food.getPrice() == null || food.getPrice() <= 0) {
            throw new IllegalArgumentException("Select an available food item from an active restaurant.");
        }
        if (order.getRestaurantName() == null
                || !order.getRestaurantName().trim().equalsIgnoreCase(
                food.getRestaurant().getRestaurantName().trim())) {
            throw new IllegalArgumentException("Food must belong to this order's restaurant.");
        }
        order.addOrderItem(new OrderItem(food, quantity));
        orderRepository.save(order);
    }

    @Transactional
    public void removeOrderItem(Long orderId, Long itemId) {
        Order order = requireOrder(orderId);
        requireEditableItems(order);
        OrderItem selected = order.getOrderItems().stream()
                .filter(item -> item != null && Objects.equals(item.getOrderItemId(), itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Order item not found."));
        order.removeOrderItem(selected);
        orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = requireOrder(id);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only unused Pending draft orders can be deleted.");
        }
        if (paymentRepository.existsByOrder_OrderId(id)
                || deliveryRepository.existsByOrderId(id)
                || feedbackRepository.existsByOrder_OrderId(id)
                || reviewTokenRepository.existsByOrder_OrderId(id)
                || trackingTokenRepository.existsByOrder_OrderId(id)) {
            throw new IllegalStateException(
                    "This order has payment, delivery, feedback or private tracking records. "
                            + "Preserve its history instead of deleting it.");
        }
        orderRepository.delete(order);
    }

    private Order requireOrder(Long id) {
        if (id == null) throw new IllegalArgumentException("Order ID is required.");
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));
    }

    private void requireEditableItems(Order order) {
        if (!canEditOrderItems(order)) {
            throw new IllegalStateException(
                    "Order items cannot be changed after payment, assignment or preparation.");
        }
    }

    private void validateDetails(Order form) {
        if (form == null || form.getRestaurantName() == null
                || form.getRestaurantName().isBlank() || form.getRestaurantName().length() > 100) {
            throw new IllegalArgumentException("Enter a valid restaurant name.");
        }
        if (form.getOrderDate() == null) {
            throw new IllegalArgumentException("Order date is required.");
        }
        if (form.getStatus() == null) {
            throw new IllegalArgumentException("Order status is required.");
        }
        String address = form.getDeliveryAddress();
        if (address == null || address.isBlank()
                || address.trim().length() < 5 || address.trim().length() > 240) {
            throw new IllegalArgumentException("Delivery address must contain 5 to 240 characters.");
        }
    }
}
