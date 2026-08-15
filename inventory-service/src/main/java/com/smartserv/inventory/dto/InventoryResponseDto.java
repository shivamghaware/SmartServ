package com.smartserv.inventory.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
