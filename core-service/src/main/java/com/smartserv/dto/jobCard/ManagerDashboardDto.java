package com.smartserv.dto.jobCard;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ManagerDashboardDto {
	private Long totalJobCards;
	private Long inProgressJobCards;
	private Long completedJobCards;
	private List<JobCardResponseDto> recentJobCards;
	
}
