package com.oop.fooddelivery.model;

import jakarta.persistence.*;

    @Entity
    @Table(name = "customers")

    public class Customer {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)

        private Long customerId;

        private String fullName;
        private String email;
        private String phoneNumber;
        private String address;
        private String status;

        public Customer() {
        }

        public Customer(String fullName, String email, String phoneNumber, String address, String status) {
            this.fullName = fullName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.address = address;
            this.status = status;
        }

        public String displayDetails() {
            return fullName + " - " + phoneNumber + " - " + status;
        }

        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
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

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }


