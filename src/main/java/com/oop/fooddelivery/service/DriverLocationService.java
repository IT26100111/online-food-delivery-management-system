
        package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.DriverAccount;
import com.oop.fooddelivery.model.DriverLocation;
import com.oop.fooddelivery.repository.DriverAccountRepository;
import com.oop.fooddelivery.repository.DriverLocationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DriverLocationService {

    private final DriverLocationRepository locationRepository;
    private final DriverAccountRepository driverRepository;

    public DriverLocationService(
            DriverLocationRepository locationRepository,
            DriverAccountRepository driverRepository) {

        this.locationRepository = locationRepository;
        this.driverRepository = driverRepository;
    }

    @Transactional
    public DriverLocation updateLocation(
            Long driverId,
            Double latitude,
            Double longitude) {

        if (latitude == null || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid latitude.");
        }

        if (longitude == null || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid longitude.");
        }

        DriverAccount driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Driver not found."));

        if (!"Active".equalsIgnoreCase(driver.getStatus())) {
            throw new IllegalArgumentException("Driver account is inactive.");
        }

        DriverLocation location = locationRepository
                .findByDriver(driver)
                .orElseGet(() ->
                        new DriverLocation(driver, latitude, longitude));

        location.setLatitude(latitude);
        location.setLongitude(longitude);

        return locationRepository.save(location);
    }

    @Transactional(readOnly = true)
    public List<DriverLocation> getAllDriverLocations() {

        return locationRepository.findAllWithDriver();
    }
}