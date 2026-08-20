package com.smartserv.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartserv.entity.Invoice;
import com.smartserv.entity.PaymentStatus;


public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    @EntityGraph(attributePaths = {"jobCard", "jobCard.appointment", "jobCard.appointment.vehicleDetails", "jobCard.appointment.vehicleDetails.customer", "jobCard.items"})
    List<Invoice> findAll();
    boolean existsByJobCardId(Long jobCardId);

    boolean existsByInvoiceNumber(String invoiceNumber);

    Invoice  findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice>  findByJobCardId(Long jobCardId);


    @EntityGraph(attributePaths = {"jobCard", "jobCard.appointment", "jobCard.appointment.vehicleDetails", "jobCard.appointment.vehicleDetails.customer", "jobCard.items"})
    List<Invoice> findByJobCard_Appointment_VehicleDetails_Customer_Id(Long customerId);

    @EntityGraph(attributePaths = {"jobCard", "jobCard.appointment", "jobCard.appointment.vehicleDetails", "jobCard.appointment.vehicleDetails.customer", "jobCard.items"})
    List<Invoice> findByPaymentStatus(PaymentStatus paymentStatus);

    long countByPaymentStatus(PaymentStatus paymentStatus);

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.paymentStatus = :status")
    Double sumTotalRevenueByStatus(@Param("status") PaymentStatus status);
}

