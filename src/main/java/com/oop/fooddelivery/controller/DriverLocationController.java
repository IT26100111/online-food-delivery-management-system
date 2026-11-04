package com.oop.fooddelivery.controller;

import com.oop.fooddelivery.model.Delivery;
import com.oop.fooddelivery.model.DriverAccount;
import com.oop.fooddelivery.model.DriverLocation;
import com.oop.fooddelivery.repository.DeliveryRepository;
import com.oop.fooddelivery.repository.DriverAccountRepository;
import com.oop.fooddelivery.repository.DriverLocationRepository;
import com.oop.fooddelivery.service.DriverLocationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class DriverLocationController {

    private final DriverLocationService locationService;
    private final DriverAccountRepository driverRepository;
    private final DeliveryRepository deliveryRepository;
    private final DriverLocationRepository driverLocationRepository;

    public DriverLocationController(
            DriverLocationService locationService,
            DriverAccountRepository driverRepository,
            DeliveryRepository deliveryRepository,
            DriverLocationRepository driverLocationRepository) {

        this.locationService = locationService;
        this.driverRepository = driverRepository;
        this.deliveryRepository = deliveryRepository;
        this.driverLocationRepository = driverLocationRepository;
    }

    // Driver sends their current GPS location
    @PostMapping("/driver/location/update")
    public ResponseEntity<?> updateDriverLocation(
            @RequestBody LocationRequest request,
            Authentication authentication) {

        if (request.latitude() == null ||
                request.longitude() == null ||
                !Double.isFinite(request.latitude()) ||
                !Double.isFinite(request.longitude()) ||
                request.latitude() < -90 ||
                request.latitude() > 90 ||
                request.longitude() < -180 ||
                request.longitude() > 180) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid GPS coordinates"));
        }

        DriverAccount driver = driverRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Driver account not found."));

        DriverLocation location = locationService.updateLocation(
                driver.getDriverId(),
                request.latitude(),
                request.longitude()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Location updated successfully",
                        "latitude", location.getLatitude(),
                        "longitude", location.getLongitude()
                )
        );
    }

    // Admin retrieves the latest GPS location for a selected delivery
    @GetMapping("/api/delivery/{deliveryId}/location")
    public ResponseEntity<?> getDeliveryLocation(
            @PathVariable Long deliveryId) {

        // Find the delivery and its assigned driver
        Delivery delivery = deliveryRepository
                .findByIdWithDriver(deliveryId)
                .orElse(null);

        if (delivery == null) {
            return ResponseEntity.notFound().build();
        }

        DriverAccount driver = delivery.getDriver();

        if (driver == null) {
            return ResponseEntity.notFound().build();
        }

        // Find the driver's latest GPS coordinates
        DriverLocation location = driverLocationRepository
                .findByDriver(driver)
                .orElse(null);

        if (location == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                new LocationResponse(
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getLastUpdated(),
                        driver.getFullName()
                )
        );
    }

    // Admin retrieves all drivers' latest locations
    @GetMapping("/admin/driver-locations")
    public List<Map<String, Object>> getAllDriverLocations() {

        return locationService.getAllDriverLocations()
                .stream()
                .map(location -> Map.<String, Object>of(
                        "driverId", location.getDriver().getDriverId(),
                        "driverName", location.getDriver().getFullName(),
                        "latitude", location.getLatitude(),
                        "longitude", location.getLongitude(),
                        "lastUpdated", location.getLastUpdated()
                ))
                .toList();
    }

    public record LocationRequest(
            Double latitude,
            Double longitude
    ) {}

    public record LocationResponse(
            Double latitude,
            Double longitude,
            LocalDateTime lastUpdated,
            String driverName
    ) {}
}