package com.smartserv.dto.invoice;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class CreatePaymentOrderResponseDto {
	private String orderId; //razorpay order id
	private Long invoiceId;
	private String invoiceNumber;
	private Double amount;
	private String currency;
	private String customerName;
	private String customerEmail;
	private String customerPhone;
	
	private String razorpayKey;
}
