package com.smartserv.service;

import java.util.List;

import com.smartserv.dto.AppointmentResponseDto;
import com.smartserv.dto.CreateAppointmentDto;
import com.smartserv.dto.UpdateAppointmentDto;
import com.smartserv.entity.Status;

public interface AppointmentService {

	AppointmentResponseDto createAppointment( CreateAppointmentDto dto);

	AppointmentResponseDto updateAppointment(Long appointmentId,  UpdateAppointmentDto dto);

	void cancelAppointment(Long appointmentId);

	List<AppointmentResponseDto> getAppointmentsByCustomerId(Long customerId);

	List<AppointmentResponseDto> getAppointmentsByVehicleId(Long vehicleId);

	AppointmentResponseDto approveAppointment(Long appointmentId);

	List<AppointmentResponseDto> getAllAppointments();

	AppointmentResponseDto getAppointmentById(Long appointmentId);

	List<AppointmentResponseDto> findPendingAppointments();

	List<AppointmentResponseDto> getAppointmentsByStatus(Status status);

	AppointmentResponseDto rejectAppointment(Long appointmentId, String rejectionReason);

	Long getPendingAppointmentCount();

	List<AppointmentResponseDto> getRsaAppointments();

	List<AppointmentResponseDto> getPendingRsaAppointments();

	List<AppointmentResponseDto> getRsaAppointmentsByStatus(Status status);

	Long getRsaCount();

}
