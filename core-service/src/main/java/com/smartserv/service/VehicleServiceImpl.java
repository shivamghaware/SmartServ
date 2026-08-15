package com.smartserv.service;

import java.util.List;
import java.util.stream.Collectors;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartserv.dto.CreateVehicleDto;
import com.smartserv.dto.VehicleResponseDto;
import com.smartserv.dto.VehicleUpdateDto;
import com.smartserv.entity.User;
import com.smartserv.entity.Vehicle;
import com.smartserv.exceptions.ResourceAlreadyExists;
import com.smartserv.exceptions.ResourceNotFoundException;
import com.smartserv.repository.UserRepository;
import com.smartserv.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

	private final VehicleRepository vehicleRepo;
	private final UserRepository userRepo;

	@Override
	public VehicleResponseDto createVehicle(CreateVehicleDto dto) {
		if (vehicleRepo.existsByLicensePlate(dto.getLicensePlate())) {
			throw new ResourceAlreadyExists("Vehicle with the license plate already exists.");
		}

		User customer = userRepo.findById(dto.getCustomerId()).orElseThrow(
				() -> new ResourceNotFoundException("Customer does not exist with the given customer id: " + dto.getCustomerId()));

		Vehicle vehicle = new Vehicle();
		vehicle.setLicensePlate(dto.getLicensePlate());
		vehicle.setBrand(dto.getBrand() != null ? dto.getBrand() : dto.getBrand());
		vehicle.setModel(dto.getModel());
		vehicle.setColor(dto.getColor());
		vehicle.setCustomer(customer);
		vehicle.setActive(true);

		Vehicle savedVehicle = vehicleRepo.save(vehicle);
		return mapVehicleToDto(savedVehicle);
	}

	@Override
	public List<VehicleResponseDto> getVehicles() {
		List<Vehicle> vehicles = vehicleRepo.findByIsActiveTrue();

		return vehicles.stream().map(this::mapVehicleToDto).collect(Collectors.toList());
	}

	@Override
	public VehicleResponseDto updateVehicle(Long vehicleId, VehicleUpdateDto dto) {
		Vehicle vehicle = vehicleRepo.findById(vehicleId)
				.orElseThrow(() -> new ResourceNotFoundException("Could not found vehicle with specified id"));
		
		if (!vehicle.isActive()) {
            throw new ResourceNotFoundException("Cannot update a deleted vehicle.");
        }

		if (dto.getBrand() != null) {
			vehicle.setBrand(dto.getBrand());
		}

		if (dto.getColor() != null) {
			vehicle.setColor(dto.getColor());
		}

		if (dto.getModel() != null) {
			vehicle.setModel(dto.getModel());
		}

		Vehicle updated = vehicleRepo.save(vehicle);
		return mapVehicleToDto(updated);
	}

	@Override
	public VehicleResponseDto getVehicleById(Long vehicleId) {
		Vehicle vehicle = vehicleRepo.findByIdAndIsActiveTrue(vehicleId)
				.orElseThrow(() -> new ResourceNotFoundException("Vehicle with specified id does not exist."));

		return mapVehicleToDto(vehicle);
	}

	@Override
	public VehicleResponseDto getVehicleByRegistration(String licensePlate) {

		Vehicle vehicle = vehicleRepo.findByLicensePlateAndIsActiveTrue(licensePlate).orElseThrow(
				() -> new ResourceNotFoundException("Vehicle with specified license plate does not exist."));

		return mapVehicleToDto(vehicle);
	}

	@Override
	public List<VehicleResponseDto> getCustomerVehicles(Long customerId) {
		if (customerId == null) return java.util.Collections.emptyList();

		User customer = userRepo.findById(customerId).orElse(null);
		if (customer == null) {
			return java.util.Collections.emptyList();
		}

		List<Vehicle> vehicles = vehicleRepo.findByCustomerIdAndIsActiveTrue(customerId);
		return vehicles.stream().map(this::mapVehicleToDto).collect(Collectors.toList());
	}

	@Override
	public VehicleResponseDto deleteVehicle(Long vehicleId) {
		Vehicle vehicle = vehicleRepo.findById(vehicleId).orElseThrow(
				() -> new ResourceNotFoundException("Vehicle does not exist for specified id: " + vehicleId));
		
		if (!vehicle.isActive()) {
            throw new ResourceNotFoundException("Vehicle is already deleted.");
        }

		vehicle.setActive(false);
		vehicleRepo.save(vehicle);

		return mapVehicleToDto(vehicle);

	}

	private VehicleResponseDto mapVehicleToDto(Vehicle vehicle) {
		VehicleResponseDto response = new VehicleResponseDto();
		response.setVehicleId(vehicle.getId());
		response.setLicensePlate(vehicle.getLicensePlate());
		response.setBrand(vehicle.getBrand());
		response.setModel(vehicle.getModel());
		response.setColor(vehicle.getColor());
		response.setActive(vehicle.isActive());

		if (vehicle.getCustomer() != null) {
			response.setCustomerId(vehicle.getCustomer().getId());
			response.setCustomerName(vehicle.getCustomer().getUserName());
			response.setCustomerEmail(vehicle.getCustomer().getEmail());
			response.setCustomerMobile(vehicle.getCustomer().getMobile());
		}
		return response;
	}

}
