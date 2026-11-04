
package com.oop.fooddelivery.model;

public class DeliveryDriver extends User {

    private String vehicleNumber;

    public DeliveryDriver(String name, String email,
                          String phoneNumber, String vehicleNumber) {

        super(name, email, phoneNumber);
        this.vehicleNumber = vehicleNumber;
    }

    @Override
    public String displayRole() {
        return "Delivery Driver";
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }
}
