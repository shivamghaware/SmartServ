package com.smartserv.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartserv.dto.jobCard.AddItemToJobCardDto;
import com.smartserv.dto.jobCard.AssignMechanicDto;
import com.smartserv.dto.jobCard.CancelJobCardDto;
import com.smartserv.dto.jobCard.CreateJobCardDto;

import com.smartserv.dto.jobCard.JobCardResponseDto;
import com.smartserv.dto.jobCard.ManagerDashboardDto;
import com.smartserv.dto.jobCard.MechanicDashboardDto;
import com.smartserv.entity.JobCardStatus;
import com.smartserv.service.JobCardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/job_cards")
@RequiredArgsConstructor
@Slf4j

public class JobCardController {
	private final JobCardService jobCardService;

	//-----------------------jobcard management-----------------------
	
	@PostMapping
	public ResponseEntity<?> createJobCard(@Valid @RequestBody CreateJobCardDto dto) {
		log.info("in create job card controller");
		return ResponseEntity.ok(jobCardService.createJobCard(dto));
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getJobCardById(@PathVariable Long id) {
		log.info("in get job card by id controller.");
		return ResponseEntity.ok(jobCardService.getJobCardById(id));

	}
	
	@GetMapping
	public ResponseEntity<?> getAllJobCards(){
		return ResponseEntity.ok(jobCardService.getAllJobCards());
	}
	
	@GetMapping("/appointment/{appointmentId}")
	public ResponseEntity<?> getJobCardByAppointmentId(@PathVariable Long appointmentId){
		log.info("in get job card by appointmentId");
		return ResponseEntity.ok(jobCardService.getJobCardByAppointmentId(appointmentId));
	}
	
	//-----------------------Mechanic management-----------------------
	
	@PutMapping("/{id}/assign_mechanic")
	public ResponseEntity<?> assignMechanic(@PathVariable Long id, @Valid @RequestBody AssignMechanicDto dto){
		log.info("assigning mechanic {} to job card {}", dto.getMechanicId(), id);
		return ResponseEntity.ok(jobCardService.updateMechanic(id, dto));
	}
	
	@PutMapping("/{id}/reassign_mechanic")
	public ResponseEntity<?> reassignMechanic(@PathVariable Long id, @Valid @RequestBody AssignMechanicDto dto){
		log.info("reassigning mechanic {} to job card {}", dto.getMechanicId(), id);
		return ResponseEntity.ok(jobCardService.updateMechanic(id, dto));
	}
	
	//-----------------------Status management-----------------------
	
	@PutMapping("/{id}/start")
	public ResponseEntity<?> startWork(@PathVariable Long id){
		log.info("started working on jobCard {}", id);
		return ResponseEntity.ok(jobCardService.startWork(id));
	}
	
	@PutMapping("/{id}/complete")
	public ResponseEntity<?> completeWork(@PathVariable Long id){
		log.info("completing job card {}", id);
		return ResponseEntity.ok(jobCardService.completeWork(id));
	}
	
	
	
	
	@DeleteMapping("/{id}/cancel")
	public ResponseEntity<?> cancelJobCard(@PathVariable Long id, @Valid @RequestBody CancelJobCardDto dto){
		return ResponseEntity.ok(jobCardService.cancelJobCard(id, dto.getReason()));
	}
	
	//-----------------------Items management-----------------------
	
	@PostMapping("/{id}/items")
	public ResponseEntity<?> addItemToJobCard(@PathVariable Long id, @RequestBody AddItemToJobCardDto dto){
		return ResponseEntity.ok(jobCardService.addItemToJobCard(id, dto));
	}
	
	
	@DeleteMapping("/{jobCardId}/items/{itemId}")
	public ResponseEntity<?> removeItemsFromJobCard(@PathVariable Long jobCardId, @PathVariable Long itemId){
		return ResponseEntity.ok(jobCardService.removeItemsFromJobCard(jobCardId, itemId));
	}
	
	@GetMapping("/{id}/items")
	public ResponseEntity<?> getJobCardItems(@PathVariable Long id){
		return ResponseEntity.ok(jobCardService.getJobCardItems(id));
	}
	

	
	//-----------------------Query Endpoints-----------------------
	
	@GetMapping("/manager/{managerId}")
	public ResponseEntity<?> getJobCardsByManager(@PathVariable Long managerId){
		return ResponseEntity.ok(jobCardService.getJobCardByManager(managerId));
	}
	
	@GetMapping("/mechanic/{mechanicId}")
	public ResponseEntity<?> getJobCardByMechanic(@PathVariable Long mechanicId){
		return ResponseEntity.ok(jobCardService.getJobCardByMechanic(mechanicId));
	}
	
	
	@GetMapping("/status/{status}")
	public ResponseEntity<?> getJobCardsByStatus(@PathVariable JobCardStatus status){
		return ResponseEntity.ok(jobCardService.getJobCardByStatus(status));
	}
	
	
	@GetMapping("/manager/{managerId}/status/{status}")
	public ResponseEntity<?> getManagerJobCardsByStatus(@PathVariable Long managerId, @PathVariable JobCardStatus status){
		return ResponseEntity.ok(jobCardService.getManagerJobCardsByStatus(managerId, status));
	}
	
	@GetMapping("/mechanic/{mechanicId}/status/{status}")
	public ResponseEntity<?> getMechanicJobCardsByStatus(@PathVariable Long mechanicId, @PathVariable JobCardStatus status){
		return ResponseEntity.ok(jobCardService.getMechanicJobCardsByStatus(mechanicId, status));
	}
	
	
	//-----------------------Statistics Endpoints--------------------------
	
	@GetMapping("/stats/total_count")
	public ResponseEntity<?> getTotalJobCardCount(){
		return ResponseEntity.ok(jobCardService.getJobCardCount());
	}
	
	
	@GetMapping("/stats/in_progress")
	public ResponseEntity<?> getInProgressJobCardCount(){
		return ResponseEntity.ok(jobCardService.getInProgressCount());
	}
	
	
	@GetMapping("/stats/completed_count")
	public ResponseEntity<?> getCompletedCount(){
		return ResponseEntity.ok(jobCardService.getCompletedCount());
	}
	
	@GetMapping("/stats/manager/{managerId}/count")
	public ResponseEntity<?> getManagerJobCardCount(@PathVariable Long managerId){
		return ResponseEntity.ok(jobCardService.getManagerJobCardCount(managerId));
	}
	
	@GetMapping("/stats/mechanic/{mechanicId}/count")
	public ResponseEntity<?> getMechanicJobCardCount(@PathVariable Long mechanicId){
		return ResponseEntity.ok(jobCardService.getMechanicJobCardCount(mechanicId));
	}
	
	
	//----------------------------Dashboard Endpoint-----------------------
	
	@GetMapping("/dashboard/manager/{managerId}")
	public ResponseEntity<?> getManagerDashboard(@PathVariable Long managerId){
		Long totalJobs = jobCardService.getManagerJobCardCount(managerId);
		Long inProgress = (long) jobCardService.getManagerJobCardsByStatus(managerId, JobCardStatus.IN_PROGRESS).size();
		Long completed = jobCardService.countManagerJobCardByStatus(managerId, JobCardStatus.COMPLETED);
		List<JobCardResponseDto> recentJobs = jobCardService.getJobCardByManager(managerId)
				.stream()
				.limit(5)
				.collect(Collectors.toList());
		
		ManagerDashboardDto dashboard = ManagerDashboardDto.builder()
				.totalJobCards(totalJobs)
				.inProgressJobCards(inProgress)
				.completedJobCards(completed)
				.recentJobCards(recentJobs)
				.build();
				
		return ResponseEntity.ok(dashboard);
	}
	
	
	@GetMapping("/dashboard/mechanic/{mechanicId}")
	public ResponseEntity<?> getMechanicDashboard(@PathVariable Long mechanicId){
		Long totalJobs = jobCardService.getMechanicJobCardCount(mechanicId);
		List<JobCardResponseDto> assignedJobs = jobCardService.getMechanicJobCardsByStatus(mechanicId, JobCardStatus.CREATED);
		List<JobCardResponseDto> inProgress = jobCardService.getMechanicJobCardsByStatus(mechanicId, JobCardStatus.IN_PROGRESS);
		Long completedJobs = jobCardService.countMechanicJobCardByStatus(mechanicId, JobCardStatus.COMPLETED);
		
		MechanicDashboardDto dashboard = MechanicDashboardDto.builder()
				.totalJobCards(totalJobs)
				.assignedJobCards(assignedJobs)
				.inProgressJobCards(inProgress)
				.completedJobCards(completedJobs)
				.build();
		
		return ResponseEntity.ok(dashboard);
	}
	
	
	
}
