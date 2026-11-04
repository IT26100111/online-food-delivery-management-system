
package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.DeliveryLocation;
import com.oop.fooddelivery.service.DeliveryLocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryLocationController {

    private final DeliveryLocationService locationService;

    public DeliveryLocationController(
            DeliveryLocationService locationService) {
        this.locationService = locationService;
    }

    // Receive driver's GPS coordinates
    @PostMapping("/{deliveryId}/location")
    public ResponseEntity<?> updateLocation(
            @PathVariable Long deliveryId,
            @RequestBody Map<String, Double> coordinates) {

        Double latitude = coordinates.get("latitude");
        Double longitude = coordinates.get("longitude");

        if (latitude == null || longitude == null ||
                !Double.isFinite(latitude) ||
                !Double.isFinite(longitude) ||
                latitude < -90 || latitude > 90 ||
                longitude < -180 || longitude > 180) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid GPS coordinates"));
        }

        DeliveryLocation location =
                locationService.updateLocation(
                        deliveryId, latitude, longitude);

        return ResponseEntity.ok(location);
    }

    // Retrieve latest driver location
    @GetMapping("/{deliveryId}/location")
    public ResponseEntity<?> getLocation(
            @PathVariable Long deliveryId) {

        return locationService.getLocation(deliveryId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
