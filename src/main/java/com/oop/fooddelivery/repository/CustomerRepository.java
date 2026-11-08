package com.oop.fooddelivery.repository;

import com.oop.fooddelivery.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Spring Data derives the search query from the method name.
    List<Customer> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullName, String email
    );

    // Existing orders must not lose their customer record.
    // Uses the already-existing orders table and customer_id foreign key.
    @Query(value = "SELECT COUNT(*) FROM orders WHERE customer_id = :customerId",
            nativeQuery = true)
    long countOrdersForCustomer(@Param("customerId") Long customerId);
}
