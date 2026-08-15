package com.smartserv.dto.inventory;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryResponseDto {
	private Long id;
	private String itemName;
	private String skuCode;
	private Double currentPrice;
	private Integer stockQuantity;
	private boolean deleted;
	private Integer version;
	
	private Boolean lowStock;
	private Boolean outOfStock;
	
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
