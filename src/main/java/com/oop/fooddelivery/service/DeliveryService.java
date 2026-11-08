
package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.Delivery;
import com.oop.fooddelivery.model.Order;
import com.oop.fooddelivery.model.OrderStatus;
import com.oop.fooddelivery.repository.DeliveryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderService orderService;

    private static final Set<String> VALID_STATUSES =
            Set.of(
                    "Pending",
                    "Assigned",
                    "Out for Delivery",
                    "Delivered",
                    "Cancelled"
            );

    public DeliveryService(
            DeliveryRepository deliveryRepository,
            OrderService orderService) {

        this.deliveryRepository = deliveryRepository;
        this.orderService = orderService;
    }

    // =========================================
    // CREATE / UPDATE DELIVERY
    // =========================================
    @Transactional
    public Delivery saveDelivery(Delivery delivery) {

        if (delivery == null ||
                delivery.getOrderId() == null) {
            throw new IllegalArgumentException(
                    "Please select a valid Order."
            );
        }

        String requestedStatus =
                delivery.getDeliveryStatus();

        if (!VALID_STATUSES.contains(requestedStatus)) {
            throw new IllegalArgumentException(
                    "Select a valid Delivery status."
            );
        }

        // Retrieve the actual Order.
        Order order = orderService.getOrderById(
                delivery.getOrderId()
        );

        if (order == null) {
            throw new IllegalArgumentException(
                    "The selected Order does not exist."
            );
        }

        // Reject changes to closed Orders.
        if (order.getStatus() == OrderStatus.CANCELLED ||
                order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException(
                    "Cannot modify delivery for a closed Order."
            );
        }

        // =====================================
        // CREATE NEW DELIVERY
        // =====================================
        if (delivery.getDeliveryId() == null) {

            if (deliveryRepository.existsByOrderId(
                    delivery.getOrderId())) {
                throw new IllegalStateException(
                        "A delivery is already assigned to this Order."
                );
            }

            // A newly created Delivery cannot
            // immediately be marked as completed.
            if (!requestedStatus.equals("Pending") &&
                    !requestedStatus.equals("Assigned")) {
                throw new IllegalStateException(
                        "New deliveries must start as Pending or Assigned."
                );
            }

            return deliveryRepository.save(delivery);
        }

        // =====================================
        // UPDATE EXISTING DELIVERY
        // =====================================
        Delivery existing = deliveryRepository
                .findById(delivery.getDeliveryId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Delivery record not found."
                        )
                );

        // Never allow the assigned Order to change.
        if (!Objects.equals(
                existing.getOrderId(),
                delivery.getOrderId())) {
            throw new IllegalStateException(
                    "The Order ID cannot be changed."
            );
        }

        String currentStatus =
                existing.getDeliveryStatus();

        // Check whether the requested Delivery
        // status transition is allowed.
        if (!isValidTransition(
                currentStatus,
                requestedStatus)) {
            throw new IllegalStateException(
                    "Invalid Delivery status transition: " +
                            currentStatus + " to " +
                            requestedStatus
            );
        }

        // =====================================
        // SYNCHRONIZE WITH ORDER STATUS
        // =====================================
        if (!currentStatus.equals(requestedStatus)) {

            switch (requestedStatus) {

                case "Out for Delivery" -> {

                    if (order.getStatus() != OrderStatus.READY) {
                        throw new IllegalStateException(
                                "Order must be Ready before dispatch."
                        );
                    }

                    if (!order.changeStatus(
                            OrderStatus.OUT_FOR_DELIVERY)) {
                        throw new IllegalStateException(
                                "Cannot update Order to Out for Delivery."
                        );
                    }
                }

                case "Delivered" -> {

                    if (order.getStatus() !=
                            OrderStatus.OUT_FOR_DELIVERY) {
                        throw new IllegalStateException(
                                "Order must be Out for Delivery first."
                        );
                    }

                    if (!order.changeStatus(
                            OrderStatus.DELIVERED)) {
                        throw new IllegalStateException(
                                "Cannot mark this Order as Delivered."
                        );
                    }
                }

                case "Cancelled" -> {

                    if (!order.changeStatus(
                            OrderStatus.CANCELLED)) {
                        throw new IllegalStateException(
                                "This Order cannot be cancelled now."
                        );
                    }
                }

                default -> {
                    // Pending and Assigned do not
                    // advance the Order status.
                }
            }

            // Save the synchronized Order.
            orderService.saveOrder(order);
        }

        // Update editable Delivery details.
        existing.setDeliveryPersonName(
                delivery.getDeliveryPersonName()
        );
        existing.setPhoneNumber(
                delivery.getPhoneNumber()
        );
        existing.setDeliveryAddress(
                delivery.getDeliveryAddress()
        );
        existing.setDeliveryDate(
                delivery.getDeliveryDate()
        );
        existing.setDeliveryStatus(requestedStatus);

        return deliveryRepository.save(existing);
    }

    // =========================================
    // DELIVERY STATUS TRANSITION RULES
    // =========================================
    private boolean isValidTransition(
            String current,
            String requested) {

        if (current == null || requested == null) {
            return false;
        }

        // No status change.
        if (current.equals(requested)) {
            return true;
        }

        return switch (current) {

            case "Pending" ->
                    requested.equals("Assigned") ||
                            requested.equals("Cancelled");

            case "Assigned" ->
                    requested.equals("Out for Delivery") ||
                            requested.equals("Cancelled");

            case "Out for Delivery" ->
                    requested.equals("Delivered");

            case "Delivered", "Cancelled" -> false;

            default -> false;
        };
    }

    // =========================================
    // READ ALL DELIVERIES
    // =========================================
    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    // =========================================
    // READ ONE DELIVERY
    // =========================================
    public Delivery getDeliveryById(Long id) {
        return deliveryRepository.findById(id).orElse(null);
    }

    // =========================================
    // FIND DELIVERY BY ORDER ID
    // =========================================
    public Delivery getDeliveryByOrderId(Long orderId) {

        return deliveryRepository
                .findFirstByOrderIdOrderByDeliveryIdDesc(orderId)
                .orElse(null);
    }

    // =========================================
    // DELETE DELIVERY
    // =========================================
    @Transactional
    public void deleteDelivery(Long id) {
        deliveryRepository.deleteById(id);
    }
}
