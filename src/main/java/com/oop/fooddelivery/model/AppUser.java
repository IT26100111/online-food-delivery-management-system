
package com.oop.fooddelivery.model;

import jakarta.persistence.*;

import java.util.Locale;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(
            nullable = false,
            unique = true,
            length = 254
    )
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private boolean enabled = true;

    protected AppUser() {
        // Required by JPA
    }

    public AppUser(
            String email,
            String passwordHash,
            UserRole role) {

        setEmail(email);
        setPasswordHash(passwordHash);

        if (role == null) {
            throw new IllegalArgumentException(
                    "User role is required."
            );
        }

        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required."
            );
        }

        String normalized = email.trim()
                .toLowerCase(Locale.ROOT);

        if (normalized.length() > 254) {
            throw new IllegalArgumentException(
                    "Email is too long."
            );
        }

        this.email = normalized;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {

        if (passwordHash == null ||
                passwordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Password hash is required."
            );
        }

        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
