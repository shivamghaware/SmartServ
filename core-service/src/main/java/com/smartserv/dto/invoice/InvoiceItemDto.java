package com.smartserv.dto.invoice;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class InvoiceItemDto {
	private String itemName;
	private Double itemPrice;
	private Integer quantity;
	private Double totalPrice;
}
