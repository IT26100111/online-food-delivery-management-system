package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.AppUser;
import com.oop.fooddelivery.repository.AppUserRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FoodFlowUserDetailsService
        implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public FoodFlowUserDetailsService(
            AppUserRepository appUserRepository) {

        this.appUserRepository = appUserRepository;
    }

    // =========================================
    // LOAD ACCOUNT FOR AUTHENTICATION
    // =========================================
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(
            String email) throws UsernameNotFoundException {

        // Find the account using its email.
        AppUser appUser = appUserRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Invalid email or password."
                        )
                );

        // Convert our AppUser into Spring Security's
        // UserDetails representation.
        return User.withUsername(appUser.getEmail())

                // This is the stored BCrypt-based hash,
                // not the customer's original password.
                .password(appUser.getPasswordHash())

                // Convert CUSTOMER -> ROLE_CUSTOMER
                // and ADMIN -> ROLE_ADMIN.
                .roles(appUser.getRole().name())

                // Prevent disabled accounts from logging in.
                .disabled(!appUser.isEnabled())

                .build();
    }
}
