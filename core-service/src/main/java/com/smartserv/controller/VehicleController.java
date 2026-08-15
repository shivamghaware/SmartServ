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

import com.smartserv.dto.CreateVehicleDto;
import com.smartserv.dto.VehicleUpdateDto;
import com.smartserv.service.VehicleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor

public class VehicleController {
	
	private final VehicleService vehicleService;
	
	@PostMapping
	public ResponseEntity<?> createVehicle(@RequestBody @Valid CreateVehicleDto dto){
		return ResponseEntity.ok(vehicleService.createVehicle(dto));
	}
	
	@GetMapping
	public ResponseEntity<?> getVehicles(){
		return ResponseEntity.ok(vehicleService.getVehicles());
	}
	
	@PutMapping("/{vehicleId}")
	public ResponseEntity<?> updateVehicle(@PathVariable Long vehicleId, @RequestBody @Valid VehicleUpdateDto dto){
		return ResponseEntity.ok(vehicleService.updateVehicle(vehicleId, dto));
	}
	
	@GetMapping("/{vehicleId}")
	public ResponseEntity<?> getVehicleById(@PathVariable Long vehicleId){
		return ResponseEntity.ok(vehicleService.getVehicleById(vehicleId));
	}
	
	@GetMapping("/license_plate/{licensePlate}")
	public ResponseEntity<?> getVehicleByLicensePlate(@PathVariable String licensePlate){
		return ResponseEntity.ok(vehicleService.getVehicleByRegistration(licensePlate));
	}
	
	@GetMapping("/customer/{customerId}")
	public ResponseEntity<?> getCustomerVehicles(@PathVariable Long customerId){
		return ResponseEntity.ok(vehicleService.getCustomerVehicles(customerId));
	}
	
	@DeleteMapping("/{vehicleId}")
	public ResponseEntity<?> deleteVehicle(@PathVariable Long vehicleId){
		return ResponseEntity.ok(vehicleService.deleteVehicle(vehicleId));
	}
}
