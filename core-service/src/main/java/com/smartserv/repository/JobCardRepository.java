package com.smartserv.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartserv.entity.Appointment;
import com.smartserv.entity.JobCard;
import com.smartserv.entity.JobCardStatus;
import com.smartserv.entity.User;


public interface JobCardRepository extends JpaRepository<JobCard, Long> {

    @Query("SELECT DISTINCT j FROM JobCard j " +
           "LEFT JOIN FETCH j.appointment a " +
           "LEFT JOIN FETCH a.vehicleDetails v " +
           "LEFT JOIN FETCH v.customer " +
           "LEFT JOIN FETCH j.manager " +
           "LEFT JOIN FETCH j.mechanic " +
           "LEFT JOIN FETCH j.items")
    List<JobCard> findAll();

    boolean existsByAppointmentId(Long appointmentId);

    JobCard findByAppointment(Appointment appointment);

    @Query("SELECT DISTINCT j FROM JobCard j " +
           "LEFT JOIN FETCH j.appointment a " +
           "LEFT JOIN FETCH a.vehicleDetails v " +
           "LEFT JOIN FETCH v.customer " +
           "LEFT JOIN FETCH j.manager " +
           "LEFT JOIN FETCH j.mechanic " +
           "LEFT JOIN FETCH j.items " +
           "WHERE j.manager = :manager")
    List<JobCard> findByManager(@Param("manager") User manager);

    @Query("SELECT DISTINCT j FROM JobCard j " +
           "LEFT JOIN FETCH j.appointment a " +
           "LEFT JOIN FETCH a.vehicleDetails v " +
           "LEFT JOIN FETCH v.customer " +
           "LEFT JOIN FETCH j.manager " +
           "LEFT JOIN FETCH j.mechanic " +
           "LEFT JOIN FETCH j.items " +
           "WHERE j.mechanic = :mechanic")
    List<JobCard> findByMechanic(@Param("mechanic") User mechanic);

    @Query("SELECT DISTINCT j FROM JobCard j " +
           "LEFT JOIN FETCH j.appointment a " +
           "LEFT JOIN FETCH a.vehicleDetails v " +
           "LEFT JOIN FETCH v.customer " +
           "LEFT JOIN FETCH j.manager " +
           "LEFT JOIN FETCH j.mechanic " +
           "LEFT JOIN FETCH j.items " +
           "WHERE j.jobCardStatus = :jobCardStatus")
    List<JobCard> findByJobCardStatus(@Param("jobCardStatus") JobCardStatus jobCardStatus);

    @Query("SELECT DISTINCT j FROM JobCard j " +
           "LEFT JOIN FETCH j.appointment a " +
           "LEFT JOIN FETCH a.vehicleDetails v " +
           "LEFT JOIN FETCH v.customer " +
           "LEFT JOIN FETCH j.manager " +
           "LEFT JOIN FETCH j.mechanic " +
           "LEFT JOIN FETCH j.items " +
           "WHERE j.manager.id = :managerId AND j.jobCardStatus = :status")
    List<JobCard> findByManagerIdAndJobCardStatus(@Param("managerId") Long managerId, @Param("status") JobCardStatus status);

    @Query("SELECT DISTINCT j FROM JobCard j " +
           "LEFT JOIN FETCH j.appointment a " +
           "LEFT JOIN FETCH a.vehicleDetails v " +
           "LEFT JOIN FETCH v.customer " +
           "LEFT JOIN FETCH j.manager " +
           "LEFT JOIN FETCH j.mechanic " +
           "LEFT JOIN FETCH j.items " +
           "WHERE j.mechanic.id = :mechanicId AND j.jobCardStatus = :status")
    List<JobCard> findByMechanicIdAndJobCardStatus(@Param("mechanicId") Long mechanicId, @Param("status") JobCardStatus status);

    long countByMechanicIdAndJobCardStatus(Long mechanicId, JobCardStatus status);

    long countByManagerIdAndJobCardStatus(Long managerId, JobCardStatus status);

    Long countByJobCardStatus(JobCardStatus status);

    Long countByManagerId(Long managerId);

    Long countByMechanicId(Long mechanicId);


}



//long countByStatus(JobCardStatus status);
//long countByManagerId(Long managerId);
//long countByMechanicId(Long mechanicId);
