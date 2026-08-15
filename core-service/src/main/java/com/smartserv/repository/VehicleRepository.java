package com.smartserv.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.smartserv.entity.Vehicle;


public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @EntityGraph(attributePaths = {"customer"})
    List<Vehicle> findAll();

    @EntityGraph(attributePaths = {"customer"})
    List<Vehicle> findByIsActiveTrue();

    boolean existsByLicensePlate(String licensePlate);

    Optional<Vehicle> findByLicensePlateAndIsActiveTrue(String licensePlate);

    @EntityGraph(attributePaths = {"customer"})
    List<Vehicle> findByCustomerIdAndIsActiveTrue(Long customerID);

    Optional<Vehicle> findByIdAndIsActiveTrue(Long id);

}

