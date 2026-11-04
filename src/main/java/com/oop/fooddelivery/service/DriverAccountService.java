package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.DriverAccount;
import com.oop.fooddelivery.repository.DriverAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class DriverAccountService {

    private final DriverAccountRepository driverAccountRepository;

    private final PasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public DriverAccountService(DriverAccountRepository driverAccountRepository) {
        this.driverAccountRepository = driverAccountRepository;
    }

    // CREATE - Save a new driver with encrypted password
    public DriverAccount saveDriver(DriverAccount driver) {
        driver.setPassword(passwordEncoder.encode(driver.getPassword()));
        return driverAccountRepository.save(driver);
    }

    // READ ALL - Get all drivers
    public List<DriverAccount> getAllDrivers() {
        return driverAccountRepository.findAll();
    }

    // READ ONE - Get driver by ID
    public DriverAccount getDriverById(Long id) {
        return driverAccountRepository.findById(id).orElse(null);
    }

    // FIND BY EMAIL
    public Optional<DriverAccount> getDriverByEmail(String email) {
        return driverAccountRepository.findByEmail(email);
    }

    // UPDATE - Update driver details
    public DriverAccount updateDriver(Long id, DriverAccount updatedDriver) {

        DriverAccount existingDriver = driverAccountRepository
                .findById(id)
                .orElse(null);

        if (existingDriver == null) {
            return null;
        }

        existingDriver.setFullName(updatedDriver.getFullName());
        existingDriver.setEmail(updatedDriver.getEmail());
        existingDriver.setPhoneNumber(updatedDriver.getPhoneNumber());
        existingDriver.setVehicleNumber(updatedDriver.getVehicleNumber());
        existingDriver.setStatus(updatedDriver.getStatus());

        // Only change password if a new password was entered
        if (updatedDriver.getPassword() != null
                && !updatedDriver.getPassword().isBlank()) {

            existingDriver.setPassword(
                    passwordEncoder.encode(updatedDriver.getPassword())
            );
        }

        return driverAccountRepository.save(existingDriver);
    }

    // DELETE - Delete driver
    public void deleteDriver(Long id) {
        driverAccountRepository.deleteById(id);
    }

    // VERIFY PASSWORD
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
