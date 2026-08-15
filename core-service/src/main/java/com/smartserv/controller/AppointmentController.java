package com.smartserv.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartserv.dto.ApproveRejectDto;
import com.smartserv.dto.CreateAppointmentDto;
import com.smartserv.dto.UpdateAppointmentDto;
import com.smartserv.entity.Status;
import com.smartserv.service.AppointmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {
	
	private final AppointmentService appointmentService;
	
	//-----CUSTOMER MAPPING-------
	@PreAuthorize("hasRole('CUSTOMER')")
	@PostMapping
	ResponseEntity<?> createAppointment(@Valid @RequestBody CreateAppointmentDto dto){
		log.info("Received request to create appointment for vehicle, {}" , dto.getVehicleId());
		return ResponseEntity.ok(appointmentService.createAppointment(dto));
	}
	
	@PreAuthorize("hasRole('CUSTOMER')")
	@PutMapping("/{appointmentId}")
	public ResponseEntity<?> updateAppointment(@PathVariable Long appointmentId, @Valid @RequestBody UpdateAppointmentDto dto){
		log.info("received update appointment request");
		return ResponseEntity.ok(appointmentService.updateAppointment(appointmentId, dto));
	}
	
	@PreAuthorize("hasRole('CUSTOMER')")
	@DeleteMapping("/{appointmentId}/cancel")
	public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId){
		appointmentService.cancelAppointment(appointmentId);
		return ResponseEntity.noContent().build();
	}
	
	
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or #customerId == authentication.credentials")
	@GetMapping("/customer/{customerId}")
	public ResponseEntity<?> getAllAppointmentsByCustomer(@PathVariable Long customerId){
		return ResponseEntity.ok(appointmentService.getAppointmentsByCustomerId(customerId));
	}
	
	@PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER', 'ADMIN')")
	@GetMapping("/vehicle/{vehicleId}")
	public ResponseEntity<?> getAppointmentsByVehice(@PathVariable Long vehicleId){
		return ResponseEntity.ok(appointmentService.getAppointmentsByVehicleId(vehicleId));
	}
	
	
	
	//----------MANAGER MAPPING----------
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping
	ResponseEntity<?> getAllAppointments(){
		return ResponseEntity.ok(appointmentService.getAllAppointments());
	}
	
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/{appointmentId}")
	ResponseEntity<?> getAppointmentById(@PathVariable Long appointmentId){
		return ResponseEntity.ok(appointmentService.getAppointmentById(appointmentId));
	}
	
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/pending")
	ResponseEntity<?> getPendingAppointments(){
		return ResponseEntity.ok(appointmentService.findPendingAppointments());
	}
	
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/status/{status}")
	ResponseEntity<?> getAppointmentsByStatus(@PathVariable Status status){
		return ResponseEntity.ok(appointmentService.getAppointmentsByStatus(status));
	}
	
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@PutMapping("{appointmentId}/approve")
	ResponseEntity<?> rejectAppointment(@PathVariable Long appointmentId){
		return ResponseEntity.ok(appointmentService.approveAppointment(appointmentId));
	}
	
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@PutMapping("{appointmentId}/reject")
	ResponseEntity<?> approveAppointment(@PathVariable Long appointmentId, @Valid @RequestBody ApproveRejectDto dto){
		System.out.println("rejection reason: "+dto.getRejectionReason());
		return ResponseEntity.ok(appointmentService.rejectAppointment(appointmentId, dto.getRejectionReason()));
	}
	
	
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/status/pending_count")
	ResponseEntity<?> getPendingAppointmentCount(){
		return ResponseEntity.ok(appointmentService.getPendingAppointmentCount());
	}
	
	
	
	//-----------RSA Mapping------------
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/rsa")
	ResponseEntity<?> getAllRsaAppointments(){
		return ResponseEntity.ok(appointmentService.getRsaAppointments());
	}
	
	
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/rsa/pending")
	ResponseEntity<?> getPendingRsaAppointments(){
		return ResponseEntity.ok(appointmentService.getPendingRsaAppointments());
	}
	
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/rsa/{status}")
	ResponseEntity<?> getRsaAppointmentsByStatus(@PathVariable Status status){
		return ResponseEntity.ok(appointmentService.getRsaAppointmentsByStatus(status));
	}
	
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/status/rsa_count")
	ResponseEntity<?> getRsaCount(){
		return ResponseEntity.ok(appointmentService.getRsaCount());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
