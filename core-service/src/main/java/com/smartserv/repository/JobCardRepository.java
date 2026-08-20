package com.smartserv.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.smartserv.entity.Appointment;
import com.smartserv.entity.JobCard;
import com.smartserv.entity.JobCardStatus;
import com.smartserv.entity.User;


public interface JobCardRepository extends JpaRepository<JobCard, Long> {

    @EntityGraph(value = "JobCard.deep", type = EntityGraph.EntityGraphType.LOAD)
    List<JobCard> findAll();

    boolean existsByAppointmentId(Long appointmentId);

    JobCard findByAppointment(Appointment appointment);

    @EntityGraph(value = "JobCard.deep", type = EntityGraph.EntityGraphType.LOAD)
    List<JobCard> findByManager(User manager);

    @EntityGraph(value = "JobCard.deep", type = EntityGraph.EntityGraphType.LOAD)
    List<JobCard> findByMechanic(User mechanic);

    @EntityGraph(value = "JobCard.deep", type = EntityGraph.EntityGraphType.LOAD)
    List<JobCard> findByJobCardStatus(JobCardStatus jobCardStatus);

    @EntityGraph(value = "JobCard.deep", type = EntityGraph.EntityGraphType.LOAD)
    List<JobCard> findByManagerIdAndJobCardStatus(Long managerId, JobCardStatus status);

    @EntityGraph(value = "JobCard.deep", type = EntityGraph.EntityGraphType.LOAD)
    List<JobCard> findByMechanicIdAndJobCardStatus(Long mechanicId, JobCardStatus status);

    long countByMechanicIdAndJobCardStatus(Long mechanicId, JobCardStatus status);

    long countByManagerIdAndJobCardStatus(Long managerId, JobCardStatus status);

    Long countByJobCardStatus(JobCardStatus status);

    Long countByManagerId(Long managerId);

    Long countByMechanicId(Long mechanicId);

}




//long countByStatus(JobCardStatus status);
//long countByManagerId(Long managerId);
//long countByMechanicId(Long mechanicId);
