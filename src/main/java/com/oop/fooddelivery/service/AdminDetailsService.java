
package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.AdminAccount;
import com.oop.fooddelivery.model.DriverAccount;
import com.oop.fooddelivery.repository.AdminAccountRepository;
import com.oop.fooddelivery.repository.DriverAccountRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminDetailsService implements UserDetailsService {

    private final AdminAccountRepository adminAccountRepository;
    private final DriverAccountRepository driverAccountRepository;

    public AdminDetailsService(
            AdminAccountRepository adminAccountRepository,
            DriverAccountRepository driverAccountRepository) {

        this.adminAccountRepository = adminAccountRepository;
        this.driverAccountRepository = driverAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        // First, check whether the account belongs to an Admin
        AdminAccount admin = adminAccountRepository.findByEmail(email)
                .orElse(null);

        if (admin != null) {
            return User.withUsername(admin.getEmail())
                    .password(admin.getPassword())
                    .roles("ADMIN")
                    .build();
        }

        // If not an Admin, check whether it belongs to a Driver
        DriverAccount driver = driverAccountRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Account not found"));

        return User.withUsername(driver.getEmail())
                .password(driver.getPassword())
                .roles("DRIVER")
                .disabled(!"Active".equalsIgnoreCase(driver.getStatus()))
                .build();
    }
}
