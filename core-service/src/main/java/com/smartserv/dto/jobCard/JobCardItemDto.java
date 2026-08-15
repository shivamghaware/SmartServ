package com.smartserv.dto.jobCard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobCardItemDto {
	private Long id;
	private String itemName;
	private Double itemPrice;
	private Integer quantity;
	private Double totalPrice;
	
}
