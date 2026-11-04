
package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.Delivery;
import com.oop.fooddelivery.repository.DeliveryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    public DeliveryService(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    // CREATE and UPDATE
    public Delivery saveDelivery(Delivery delivery) {
        return deliveryRepository.save(delivery);
    }

    // READ - get all deliveries for Admin
    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    // READ - get deliveries assigned to a specific Driver
    public List<Delivery> getDeliveriesForDriver(String email) {
        return deliveryRepository.findByDriver_Email(email);
    }

    // READ - get one delivery
    public Delivery getDeliveryById(Long id) {
        return deliveryRepository.findById(id).orElse(null);
    }

    // DELETE
    public void deleteDelivery(Long id) {
        deliveryRepository.deleteById(id);
    }
}
