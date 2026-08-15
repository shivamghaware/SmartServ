package com.smartserv.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleResponseDto {

	private Long vehicleId;
	private String licensePlate;
	private String brand;
	private String model;
	private String color;
	
	@JsonProperty("active")
	private boolean active;
	
	private Long customerId;
	private String customerName;
	private String customerEmail;
	private String customerMobile;
}
