package com.oop.fooddelivery.model;

public enum PaymentMethod {
    CASH("Cash"),
    CARD("Card (simulation)");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
