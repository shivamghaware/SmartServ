package com.smartserv.service;

import java.util.List;

import com.smartserv.dto.jobCard.AddItemToJobCardDto;
import com.smartserv.dto.jobCard.AssignMechanicDto;
import com.smartserv.dto.jobCard.CreateJobCardDto;

import com.smartserv.dto.jobCard.JobCardResponseDto;
import com.smartserv.entity.JobCardStatus;

import jakarta.validation.Valid;

public interface JobCardService {

	JobCardResponseDto createJobCard(@Valid CreateJobCardDto dto);

	JobCardResponseDto getJobCardById(Long jobCardId);

	List<JobCardResponseDto> getAllJobCards();

	JobCardResponseDto getJobCardByAppointmentId(Long appointmentId);

	JobCardResponseDto updateMechanic(Long jobCardId, AssignMechanicDto dto);

	JobCardResponseDto startWork(Long jobCardId);

	JobCardResponseDto completeWork(Long jobCardId);

	JobCardResponseDto cancelJobCard(Long jobCardId, String reason);

	JobCardResponseDto addItemToJobCard(Long jobCardId, AddItemToJobCardDto dto);

	JobCardResponseDto removeItemsFromJobCard(Long jobCardId, Long itemId);

	JobCardResponseDto getJobCardItems(Long jobCardId);


	List<JobCardResponseDto> getJobCardByManager(Long managerId);

	List<JobCardResponseDto> getJobCardByMechanic(Long mechanicId);

	List<JobCardResponseDto> getJobCardByStatus(JobCardStatus status);

	List<JobCardResponseDto> getManagerJobCardsByStatus(Long managerId, JobCardStatus status);

	List<JobCardResponseDto> getMechanicJobCardsByStatus(Long mechanicId, JobCardStatus status);

	Long getJobCardCount();

	Long getInProgressCount();

	Long getCompletedCount();

	Long getManagerJobCardCount(Long managerId);

	Long getMechanicJobCardCount(Long mechanicId);

	Long countMechanicJobCardByStatus(Long mechanicId, JobCardStatus status);

	Long countManagerJobCardByStatus(Long managerId, JobCardStatus status);


}
