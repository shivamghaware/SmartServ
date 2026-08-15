package com.smartserv.dto.jobCard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.smartserv.entity.JobCardStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobCardResponseDto {
	
	private Long id;
	
	private Long appointmentId;
	private String problemDescription;
	private LocalDate appointmentDate;
	
	private Long vehicleId;
	private String licensePlate;
	private String brand;
	private String model;
	
	private Long customerId;
	private String customerName;
	private String customerPhone;
	
	private Long managerId;
	private String managerName;
	
	private Long mechanicId;
	private String mechanicName;
	
	private JobCardStatus status;
	private String cancellationReason;
	private LocalDate estimatedCompletionDate;
	private LocalDateTime startTime;
	private LocalDateTime completionTime;
	
	private List<JobCardItemDto> items;
	
	private Double totalAmount;
	
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
