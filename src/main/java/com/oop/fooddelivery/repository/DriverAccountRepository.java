package com.oop.fooddelivery.repository;

import com.oop.fooddelivery.model.DriverAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverAccountRepository extends JpaRepository<DriverAccount, Long> {

    Optional<DriverAccount> findByEmail(String email);
}
