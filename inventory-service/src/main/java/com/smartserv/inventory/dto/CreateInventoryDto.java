package com.smartserv.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateInventoryDto {
    @NotBlank(message = "item name cannot be blank.")
    @Size(max = 200, message = "Item name cannot exceed 200 characters")
    private String itemName;
    
    @NotBlank(message = "sku code cannot be blank.")
    private String skuCode;
    
    @NotNull(message = "price is required.")
    @DecimalMin(value = "0.01", message="Price must be greater than zero.")
    private Double currentPrice;
    
    @NotNull(message = "stock quantity is required.")
    @Min(value=0, message = "quantity cannot be negative.")
    private Integer stockQuantity;
}
