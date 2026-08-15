package com.smartserv.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateInventoryDto {
    @Size(max = 200, message = "Item name cannot exceed 200 characters")
    private String itemName;

    @DecimalMin(value="0.01", message = "price cannot be negative.")
    private Double currentPrice;
    
    @Min(value=0, message = "quantity must be positive.")
    private Integer stockQuantity;
}
