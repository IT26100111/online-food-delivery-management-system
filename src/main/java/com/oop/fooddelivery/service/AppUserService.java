
package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.AppUser;
import com.oop.fooddelivery.model.UserRole;
import com.oop.fooddelivery.repository.AppUserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder) {

        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================
    // REGISTER CUSTOMER
    // =========================================
    @Transactional
    public AppUser registerCustomer(
            String email,
            String password) {

        // Validate email.
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required."
            );
        }

        String normalizedEmail = email.trim()
                .toLowerCase(Locale.ROOT);

        if (normalizedEmail.length() > 254 ||
                !normalizedEmail.matches(
                        "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            throw new IllegalArgumentException(
                    "Enter a valid email address."
            );
        }

        // Validate password.
        if (password == null ||
                password.isBlank() ||
                password.length() < 8 ||
                password.getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters " +
                            "and be no longer than 72 bytes."
            );
        }

        // Prevent duplicate accounts.
        if (appUserRepository.existsByEmailIgnoreCase(
                normalizedEmail)) {

            throw new IllegalArgumentException(
                    "An account with this email already exists."
            );
        }

        // Encrypt the password.
        String encodedPassword =
                passwordEncoder.encode(password);

        // Public registration always creates CUSTOMER.
        AppUser user = new AppUser(
                normalizedEmail,
                encodedPassword,
                UserRole.CUSTOMER
        );

        // Save the account to MySQL.
        return appUserRepository.save(user);
    }

    // =========================================
    // FIND ACCOUNT BY EMAIL
    // =========================================
    @Transactional(readOnly = true)
    public Optional<AppUser> findByEmail(String email) {

        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        return appUserRepository.findByEmailIgnoreCase(
                email.trim()
        );
    }
}
