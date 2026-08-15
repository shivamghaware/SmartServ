package com.smartserv.dto.invoice;

import java.time.LocalDateTime;
import java.util.List;

import com.smartserv.entity.PaymentStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceResponseDto {
	private Long id;
	private String  invoiceNumber;
	
	private Long jobCardId;
	private String jobCardStatus;
	
	private Long customerId;
	private String customerName;
	private String customerEmail;
	private String customerPhone;
	
	private String vehicleRegistration;
	private String vehicleBrand;
	private String vehicleModel;
	
	
	private Double baseAmount;
	private Double taxPercentage;
	private Double taxAmount;
	private Double totalAmount;
	
	private PaymentStatus paymentStatus;
	private String razorpayOrderId;
	private String razorpayPaymentId;
	private String paymentMethod;
	private LocalDateTime paidAt;
	
	private List<InvoiceItemDto> items;
	
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	
}
