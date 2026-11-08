package com.oop.fooddelivery.model;

public enum PaymentStatus {
    PENDING("Pending"),
    PAID("Paid"),
    FAILED("Failed");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canTransitionTo(PaymentStatus next) {
        if (next == null) return false;
        if (this == next) return true;
        return this == PENDING && (next == PAID || next == FAILED);
    }
}
