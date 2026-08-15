package com.smartserv.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CreateVehicleDto {
	
	@NotBlank(message="Vehicle license plate is required.")
	private String licensePlate;
	
	@NotBlank(message="Vehicle brand is required.")
	private String brand;
	
	@NotBlank(message="Vehicle model is required.")
	private String model;
	
	@NotBlank(message="Vehicle color is required.")
	private String color;
	
	@NotNull(message="Customer id cannot be null.")
	private Long customerId;
}
