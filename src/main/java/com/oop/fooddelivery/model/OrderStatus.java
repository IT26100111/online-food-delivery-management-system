package com.oop.fooddelivery.model;

public enum OrderStatus {
    PENDING("Pending"),
    PREPARING("Preparing"),
    READY("Ready"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String displayName;

    OrderStatus(String displayName){
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }
    public boolean canTransitionTo(OrderStatus newStatus){
        if(newStatus == null){
            return false;
        }
        if(this == newStatus){
            return true;
        }

        return switch(this){
            case PENDING ->
                newStatus == PREPARING ||
                newStatus == CANCELLED;
            case PREPARING ->
                newStatus == READY ||
                newStatus == CANCELLED;
            case READY ->
                newStatus == OUT_FOR_DELIVERY ||
                newStatus == CANCELLED;
            case OUT_FOR_DELIVERY ->
                newStatus == DELIVERED;
            case DELIVERED,CANCELLED ->
                false;
        };
    }
}
