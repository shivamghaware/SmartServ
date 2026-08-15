package com.smartserv.service;

import java.util.List;

import com.smartserv.dto.CreateVehicleDto;
import com.smartserv.dto.VehicleResponseDto;
import com.smartserv.dto.VehicleUpdateDto;

public interface VehicleService {

	VehicleResponseDto createVehicle(CreateVehicleDto dto);

	List<VehicleResponseDto> getVehicles();

	VehicleResponseDto updateVehicle(Long vehicleId, VehicleUpdateDto dto);

	VehicleResponseDto getVehicleById(Long vehicleId);

	VehicleResponseDto getVehicleByRegistration(String licensePlate);

	List<VehicleResponseDto> getCustomerVehicles(Long customerId);

	VehicleResponseDto deleteVehicle(Long vehicleId);

}
