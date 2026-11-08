
package com.oop.fooddelivery.checkout;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CheckoutForm {

    @NotBlank(message = "Full name is required.")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "Email is required.")
    @Email(message = "Enter a valid email address.")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "Phone number is required.")
    @Pattern(
            regexp = "^0[0-9]{9}$",
            message = "Enter a valid 10-digit Sri Lankan phone number."
    )
    private String phoneNumber;

    @NotBlank(message = "Delivery address is required.")
    @Size(
            min = 5,
            max = 240,
            message = "Delivery address must contain 5 to 240 characters."
    )
    private String deliveryAddress;

    public CheckoutForm() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
