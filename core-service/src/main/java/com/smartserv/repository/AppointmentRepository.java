package com.smartserv.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.smartserv.entity.Appointment;
import com.smartserv.entity.Status;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @EntityGraph(attributePaths = {"vehicleDetails", "vehicleDetails.customer"})
    List<Appointment> findAll();

    List<Appointment> findByVehicleDetails_Customer_Id(Long customerId);

    List<Appointment> findByVehicleDetails_Id(Long vehicleId);

    @EntityGraph(attributePaths = {"vehicleDetails", "vehicleDetails.customer"})
    List<Appointment> findByStatus(Status status);

    @Query("SELECT COUNT(*) FROM Appointment WHERE status='PENDING'")
    Long countPendingAppointments();

    @EntityGraph(attributePaths = {"vehicleDetails", "vehicleDetails.customer"})
    List<Appointment> findByRsaTrue();

    @EntityGraph(attributePaths = {"vehicleDetails", "vehicleDetails.customer"})
    List<Appointment> findByRsaTrueAndStatus(Status status);

    @Query("SELECT COUNT(*) FROM Appointment WHERE rsa=true")
    Long countRsaAppointment();
}

