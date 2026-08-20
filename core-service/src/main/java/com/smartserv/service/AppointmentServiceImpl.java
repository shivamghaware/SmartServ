package com.smartserv.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartserv.dto.AppointmentResponseDto;
import com.smartserv.dto.CreateAppointmentDto;
import com.smartserv.dto.RsaLocationDto;
import com.smartserv.dto.UpdateAppointmentDto;
import com.smartserv.entity.Appointment;
import com.smartserv.entity.Status;
import com.smartserv.entity.User;
import com.smartserv.entity.Vehicle;
import com.smartserv.exceptions.InvalidDateException;
import com.smartserv.exceptions.InvalidOperationException;
import com.smartserv.exceptions.ResourceNotFoundException;
import com.smartserv.repository.AppointmentRepository;
import com.smartserv.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j

public class AppointmentServiceImpl implements AppointmentService {
	
	private final VehicleRepository vehicleRepo;
	private final AppointmentRepository appointmentRepo;

	@Override
	public AppointmentResponseDto createAppointment( CreateAppointmentDto dto) {
		log.info("creating {} appointment for vehicleId: {}", Boolean.TRUE.equals(dto.isRsa()) ? "RSA" : "Regular", dto.getVehicleId());
		
		Vehicle vehicle = vehicleRepo.findByIdAndIsActiveTrue(dto.getVehicleId()).orElseThrow(()-> new ResourceNotFoundException("Vehicle not found."));
		
		if (!Boolean.TRUE.equals(dto.isRsa()) && dto.getRequestDate().isBefore(LocalDate.now())) {
	            throw new InvalidDateException("Appointment date cannot be in the past");
	        }
		
		if(Boolean.TRUE.equals(dto.isRsa())) {
			validateRsaCoordinates(dto.getRsaCoordinates());
		}
		
		Appointment appointment = new Appointment();
		appointment.setVehicleDetails(vehicle);
		appointment.setRequestDate(dto.getRequestDate());
		appointment.setScheduledTime(dto.getScheduledTime());
		appointment.setProblemDescription(dto.getDescription());
		appointment.setCustomerPhotoUrl(dto.getCustomerPhotoUrl());
		appointment.setRsa(Boolean.TRUE.equals(dto.isRsa()));
		appointment.setRsaCoordinates(dto.getRsaCoordinates());		
		appointment.setStatus(Status.PENDING);
		
		Appointment saved = appointmentRepo.save(appointment);
		
		log.info("Appointment created successfully with id: {}", saved.getId());
		
		if(saved.isRsa()) {
			log.warn("RSA alert : Emergency appointment created at coordinates : {}", saved.getRsaCoordinates());
		}
		
		return mapToResponseDto(saved);
	}
	
	
	@Override
	public AppointmentResponseDto updateAppointment(Long appointmentId, UpdateAppointmentDto dto) {

		Appointment appointment = appointmentRepo.findById(appointmentId).orElseThrow(()->  new ResourceNotFoundException("appointment not found"));
		
		 if (appointment.isRsa()) {
	            throw new InvalidOperationException(
	                "RSA appointments cannot be modified. Please contact support."
	            );
	        }
	        
	  
	        if (appointment.getStatus() != Status.PENDING) {
	            throw new InvalidOperationException(
	                "Cannot update appointment with status: " + appointment.getStatus()
	            );
	        }
	        
	        
	        if (dto.getRequestDate() != null) {
	            if (dto.getRequestDate().isBefore(LocalDate.now())) {
	                throw new InvalidDateException("Appointment date cannot be in the past");
	            }
	            appointment.setRequestDate(dto.getRequestDate());
	        }
	        
	        if (dto.getDescription() != null) {
	            appointment.setProblemDescription(dto.getDescription());
	        }
	        
	        if (dto.getCustomerPhotoUrl() != null) {
	            appointment.setCustomerPhotoUrl(dto.getCustomerPhotoUrl());
	        }
	        
	        Appointment updated = appointmentRepo.save(appointment);
	        
	        return mapToResponseDto(updated);
	}
	
	
	@Override
	public void cancelAppointment(Long appointmentId) {
		Appointment appointment = appointmentRepo.findById(appointmentId).orElseThrow(()-> new ResourceNotFoundException("appointment not found"));
		if(appointment.getStatus() != Status.PENDING) {
			throw new InvalidOperationException("cannot cancel appointment with status: "+ appointment.getStatus());
		}
		
		appointment.setStatus(Status.REJECTED);
		appointmentRepo.save(appointment);
		
		log.info("appointment {} cancelled by customer.", appointmentId);
	}
	
	
	@Override
	public List<AppointmentResponseDto> getAppointmentsByCustomerId(Long customerId) {
		List<Appointment> appointments = appointmentRepo.findByVehicleDetails_Customer_Id(customerId);
		
		return appointments.stream().map(this::mapToResponseDto).collect(Collectors.toList());
	}
	
	
	@Override
	public List<AppointmentResponseDto> getAppointmentsByVehicleId(Long vehicleId) {
		List<Appointment> appointments = appointmentRepo.findByVehicleDetails_Id(vehicleId);
		
		return appointments.stream().map(this::mapToResponseDto).collect(Collectors.toList());
	}
	
	
	
	
	
	
	@Override
	public List<AppointmentResponseDto> getAllAppointments() {
		List<Appointment> appointments = appointmentRepo.findAll();
		return appointments.stream().map(this::mapToResponseDto).collect(Collectors.toList());
	}
	
	@Override
	public AppointmentResponseDto getAppointmentById(Long appointmentId) {
		Appointment appointment = appointmentRepo.findById(appointmentId).orElseThrow(()-> new ResourceNotFoundException("appointment not found"));
		
		
		return mapToResponseDto(appointment);
	}
	
	
	@Override
	public List<AppointmentResponseDto> findPendingAppointments() {
		List<Appointment> pendingAppointments = appointmentRepo.findByStatus(Status.PENDING);
		
		return pendingAppointments.stream().map(this::mapToResponseDto).collect(Collectors.toList());
	}
	
	@Override
	public List<AppointmentResponseDto> getAppointmentsByStatus(Status status) {
		List<Appointment> appointments = appointmentRepo.findByStatus(status);
		
		return appointments.stream().map(this::mapToResponseDto).collect(Collectors.toList());
	}
	
	@Override
	public AppointmentResponseDto approveAppointment(Long appointmentId) {
		Appointment appointment = appointmentRepo.findById(appointmentId).orElseThrow(()-> new ResourceNotFoundException("appointment not found"));
		
		if(appointment.getStatus() != Status.PENDING) {
			throw new InvalidOperationException("Only pending appointments can be approved.");
		}
		
		appointment.setStatus(Status.APPROVED);
		Appointment approved = appointmentRepo.save(appointment);
		log.info("Appointment {} approved by manager", appointmentId);
		return mapToResponseDto(approved);
	}
	
	@Override
	public AppointmentResponseDto rejectAppointment(Long appointmentId, String rejectionReason) {
		Appointment appointment = appointmentRepo.findById(appointmentId).orElseThrow(()-> new ResourceNotFoundException("appointment not found"));
		
		
		
		if(appointment.getStatus() != Status.PENDING) {
			throw new InvalidOperationException("only pending appointments can be rejected");
		}
		
		if(rejectionReason == null || rejectionReason.trim().isEmpty()) {
			throw new IllegalArgumentException("Rejection reason is required.");
		}
		
		appointment.setStatus(Status.REJECTED);
		appointment.setRejectionReason(rejectionReason);
		
		Appointment rejected = appointmentRepo.save(appointment);
		log.info("Appointment {} rejected by manager. Reason {} ", appointmentId, rejectionReason);
		return mapToResponseDto(rejected);
	}
	
	@Override
	public Long getPendingAppointmentCount() {
		
		return appointmentRepo.countByStatus(Status.PENDING);
	}
	
	@Override
	public List<AppointmentResponseDto> getRsaAppointments() {
		List<Appointment> rsaAppointments = appointmentRepo.findByRsaTrue();
		
		log.info("Found {} RSA appointments", rsaAppointments.size());
		
		return rsaAppointments.stream().map(this::mapToResponseDto).collect(Collectors.toList());
	}	
	
	@Override
	public List<AppointmentResponseDto> getPendingRsaAppointments() {
		List<Appointment> appointments = appointmentRepo.findByRsaTrueAndStatus(Status.PENDING);
		
		return appointments.stream().map(this::mapToResponseDto).collect(Collectors.toList());
	}
	
	@Override
	public List<AppointmentResponseDto> getRsaAppointmentsByStatus(Status status){
		List<Appointment> appointments = appointmentRepo.findByRsaTrueAndStatus(status);
		
		return appointments.stream().map(this::mapToResponseDto).collect(Collectors.toList());
	}
	
	@Override
	public Long getRsaCount() {
		return appointmentRepo.countByRsaTrue();
	}

	private void validateRsaCoordinates(String coordinates) {
		if(coordinates == null || coordinates.trim().isEmpty()) {
			throw new IllegalArgumentException("RSA location or coordinates required.");
		}
		
		RsaLocationDto location = RsaLocationDto.fromCoordinates(coordinates);
		if(location != null) {
			if(location.getLatitude() < -90 || location.getLatitude() > 90) {
				throw new IllegalArgumentException("latitude must be between -90 and 90.");
			}
			if(location.getLongitude() < -180 || location.getLongitude() > 180) {
				throw new IllegalArgumentException("longitude must be between -180 and 180.");
			}
		}
	}
	
	private AppointmentResponseDto mapToResponseDto(Appointment appointment) {
		if (appointment == null) return null;

		Vehicle vehicle = appointment.getVehicleDetails();
		User customer = vehicle != null ? vehicle.getCustomer() : null;
		
		return AppointmentResponseDto.builder()
				.id(appointment.getId())
				.vehicleId(vehicle != null ? vehicle.getId() : null)
				.licensePlate(vehicle != null ? vehicle.getLicensePlate() : "")
				.brand(vehicle != null ? vehicle.getBrand() : "")
				.model(vehicle != null ? vehicle.getModel() : "")
				.color(vehicle != null ? vehicle.getColor() : "")
				.customerId(customer != null ? customer.getId() : null)
				.customerName(customer != null ? customer.getUserName() : "Customer")
				.email(customer != null ? customer.getEmail() : "")
				.mobile(customer != null ? customer.getMobile() : "")
				.requestDate(appointment.getRequestDate())
				.scheduledTime(appointment.getScheduledTime())
				.problemDescription(appointment.getProblemDescription())
				.customerPhotoUrl(appointment.getCustomerPhotoUrl())
				.isRsa(appointment.isRsa())
				.rsaCoordinates(appointment.getRsaCoordinates())
				.rsaLocation(RsaLocationDto.fromCoordinates(appointment.getRsaCoordinates()))
				.status(appointment.getStatus())
				.rejectionReason(appointment.getRejectionReason())
				.createdAt(appointment.getCreatedOn())
				.updatedAt(appointment.getLastUpdated())
				.build();
	}
}
