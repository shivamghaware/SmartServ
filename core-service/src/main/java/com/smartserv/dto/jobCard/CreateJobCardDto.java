package com.smartserv.dto.jobCard;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
	public class CreateJobCardDto {
		
		@NotNull(message="appointment id cannot be null.")
		private Long appointmentId;
		
		@NotNull(message="manager id cannot be null.")
		private Long managerId;
		
		private Long mechanicId;
		
		private LocalDate estimatedCompletionDate;
	}
