package com.smartserv.dto.jobCard;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddItemToJobCardDto {
	@NotNull(message="Inventory item is required")
	private Long inventoryItemId;
	
	
	@NotNull(message="Quantity is required.")
	@Min(value=1, message = "Quantity must be greater than one.")
	private Integer quantity;


}
