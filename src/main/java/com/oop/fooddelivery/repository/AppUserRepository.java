
package com.oop.fooddelivery.repository;

import com.oop.fooddelivery.model.AppUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository
        extends JpaRepository<AppUser, Long> {

    // Find a login account by email.
    Optional<AppUser> findByEmailIgnoreCase(String email);

    // Check whether an email is already registered.
    boolean existsByEmailIgnoreCase(String email);
}
