
package com.oop.fooddelivery.repository;

import com.oop.fooddelivery.model.DeliveryLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryLocationRepository
        extends JpaRepository<DeliveryLocation, Long> {

    Optional<DeliveryLocation> findByDeliveryId(Long deliveryId);
}
