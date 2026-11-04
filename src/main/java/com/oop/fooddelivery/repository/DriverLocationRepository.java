package com.oop.fooddelivery.repository;

import com.oop.fooddelivery.model.DriverLocation;
import com.oop.fooddelivery.model.DriverAccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverLocationRepository
        extends JpaRepository<DriverLocation, Long> {

    Optional<DriverLocation> findByDriver(DriverAccount driver);

    @Query("""
           SELECT location
           FROM DriverLocation location
           JOIN FETCH location.driver
           """)
    List<DriverLocation> findAllWithDriver();

}
