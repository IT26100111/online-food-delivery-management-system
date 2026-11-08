package com.oop.fooddelivery.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @NotBlank(message = "Full name is required.")
    @Size(max = 100, message = "Full name must be at most 100 characters.")
    private String fullName;

    @NotBlank(message = "Email is required.")
    @Email(message = "Enter a valid email address.")
    @Size(max = 255, message = "Email must be at most 255 characters.")
    private String email;

    @NotBlank(message = "Phone number is required.")
    @Pattern(
            regexp = "^0[0-9]{9}$",
            message = "Enter a valid 10-digit Sri Lankan phone number."
    )
    private String phoneNumber;

    @NotBlank(message = "Address is required.")
    @Size(min = 5, max = 240,
            message = "Address must contain 5 to 240 characters.")
    private String address;

    @NotBlank(message = "Status is required.")
    @Pattern(
            regexp = "^(Active|Pending|Inactive)$",
            message = "Choose Active, Pending or Inactive."
    )
    private String status;

    // JPA needs a no-argument constructor.
    public Customer() {
    }

    // CheckoutService also uses this constructor to save guest customers.
    public Customer(String fullName, String email, String phoneNumber,
                    String address, String status) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.status = status;
    }

    public String displayDetails() {
        return fullName + " - " + phoneNumber + " - " + status;
    }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
