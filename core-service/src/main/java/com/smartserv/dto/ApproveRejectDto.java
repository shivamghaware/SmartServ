package com.smartserv.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApproveRejectDto {
	@Size(max=200, message="Rejection reason is required")
	private String rejectionReason;
}
