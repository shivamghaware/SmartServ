package com.smartserv.dto.invoice;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentVerificationResponseDto {
	private Boolean verified;
	private String message;
	private InvoiceResponseDto invoice;
}
