package com.oop.fooddelivery.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deliveryId;

    @NotNull(message = "Order ID is required.")
    @Positive(message = "Order ID must be greater than zero.")
    private Long orderId;
    @NotBlank(message = "Delivery person is required.")
    @Size(max = 255, message = "Delivery person must be at most 255 characters.")
    private String deliveryPersonName;
    @NotBlank(message = "Phone number is required.")
    @Size(max = 255, message = "Phone number must be at most 255 characters.")
    private String phoneNumber;
    @NotBlank(message = "Delivery address is required.")
    @Size(max = 255, message = "Delivery address must be at most 255 characters.")
    private String deliveryAddress;
    @NotNull(message = "Delivery date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate deliveryDate;
    @NotBlank(message = "Delivery status is required.")
    @Pattern(regexp = "Pending|Assigned|Out for Delivery|Delivered|Cancelled", message = "Select a valid delivery status.")
    private String deliveryStatus;

    // Empty constructor
    public Delivery() {
    }

    // Constructor
    public Delivery(Long orderId,
                    String deliveryPersonName,
                    String phoneNumber,
                    String deliveryAddress,
                    LocalDate deliveryDate,
                    String deliveryStatus) {

        this.orderId = orderId;
        this.deliveryPersonName = deliveryPersonName;
        this.phoneNumber = phoneNumber;
        this.deliveryAddress = deliveryAddress;
        this.deliveryDate = deliveryDate;
        this.deliveryStatus = deliveryStatus;
    }

    // Getters and Setters

    public Long getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(Long deliveryId) {
        this.deliveryId = deliveryId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getDeliveryPersonName() {
        return deliveryPersonName;
    }

    public void setDeliveryPersonName(String deliveryPersonName) {
        this.deliveryPersonName = deliveryPersonName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
}