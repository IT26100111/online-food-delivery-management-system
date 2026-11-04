
package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.DeliveryLocation;
import com.oop.fooddelivery.repository.DeliveryLocationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DeliveryLocationService {

    private final DeliveryLocationRepository locationRepository;

    public DeliveryLocationService(DeliveryLocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    // Save or update the latest driver location
    public DeliveryLocation updateLocation(
            Long deliveryId, Double latitude, Double longitude) {

        Optional<DeliveryLocation> existing =
                locationRepository.findByDeliveryId(deliveryId);

        DeliveryLocation location = existing.orElse(
                new DeliveryLocation(deliveryId, latitude, longitude)
        );

        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setUpdatedAt(LocalDateTime.now());

        return locationRepository.save(location);
    }

    // Get the latest location for a delivery
    public Optional<DeliveryLocation> getLocation(Long deliveryId) {
        return locationRepository.findByDeliveryId(deliveryId);
    }
}

