package com.smartserv.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAppointmentDto {
	
	@NotNull(message = "Vehicle id cannot be null.")
	private Long vehicleId;
	
	@NotNull(message = "Appointment date cannot be null.")
	@FutureOrPresent(message = "Appointment date must be today or in the future.")
	private LocalDate requestDate;
	
	private LocalTime scheduledTime;
	
	@NotBlank(message = "Description cannot be blank.")
	@Size(max=500, message= "Description cannot be greater than 500.")
	private String description;
	
	private boolean rsa;
	
	private String customerPhotoUrl;
	
	private String rsaCoordinates;
	
	@AssertTrue(message = "RSA coordinates are required for roadside assistance")
	private boolean isDataValid() {
		if(Boolean.TRUE.equals(rsa)) {
			return rsaCoordinates != null && !rsaCoordinates.trim().isEmpty();
		}
		return true;
	}

}
