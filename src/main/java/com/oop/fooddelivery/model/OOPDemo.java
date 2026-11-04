
package com.oop.fooddelivery.model;

public class OOPDemo {

    public static void main(String[] args) {

        User user = new DeliveryDriver(
                "Kamal",
                "kamal@gmail.com",
                "0771234567",
                "ABC-1234"
        );

        System.out.println("Name: " + user.getName());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Phone: " + user.getPhoneNumber());
        System.out.println("Role: " + user.displayRole());
    }
}
