package com.smartserv.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAppointmentDto {
	private LocalDate requestDate;
	
	@Size(max=500, message="Description cannot exceed 500 words.")
	private String description;
	
	private String customerPhotoUrl;
}
