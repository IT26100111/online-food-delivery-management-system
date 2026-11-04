package com.oop.fooddelivery.repository;

import com.oop.fooddelivery.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    // Find deliveries assigned to a particular driver
    List<Delivery> findByDriver_Email(String email);

    // Find a delivery together with its assigned driver
    @Query("""
           SELECT d
           FROM Delivery d
           LEFT JOIN FETCH d.driver
           WHERE d.deliveryId = :id
           """)
    Optional<Delivery> findByIdWithDriver(@Param("id") Long id);
}