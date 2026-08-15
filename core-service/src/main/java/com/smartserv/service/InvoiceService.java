package com.smartserv.service;

import java.util.List;

import com.smartserv.dto.invoice.CreatePaymentOrderResponseDto;
import com.smartserv.dto.invoice.InvoiceResponseDto;
import com.smartserv.dto.invoice.PaymentVerificationResponseDto;
import com.smartserv.dto.invoice.VerifyPaymentRequestDto;
import com.smartserv.entity.PaymentStatus;

public interface InvoiceService {

	InvoiceResponseDto generateInvoice(Long jobCardId);

	InvoiceResponseDto getInvoice(Long invoiceId);

	InvoiceResponseDto getInvoiceByNumber(String invoiceNumber);

	InvoiceResponseDto getInvoiceByJobCard(Long jobCardId);

	List<InvoiceResponseDto> getAllInvoices();

	List<InvoiceResponseDto> getInvoicesByCustomerId(Long customerId);

	List<InvoiceResponseDto> getInvoicesByStatus(PaymentStatus status);

	CreatePaymentOrderResponseDto createPaymentDto(Long invoiceId);

	PaymentVerificationResponseDto verifyPayment(Long invoiceId,  VerifyPaymentRequestDto request);

	long getTotalInvoicesCount();

	long getPendingPaymentCount();

	long getPaidInvoicesCount();

	Double getTotalRevenue();

}
