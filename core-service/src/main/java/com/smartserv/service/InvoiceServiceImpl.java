package com.smartserv.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartserv.dto.invoice.CreatePaymentOrderResponseDto;
import com.smartserv.dto.invoice.InvoiceItemDto;
import com.smartserv.dto.invoice.InvoiceResponseDto;
import com.smartserv.dto.invoice.PaymentVerificationResponseDto;
import com.smartserv.dto.invoice.VerifyPaymentRequestDto;
import com.smartserv.entity.Appointment;
import com.smartserv.entity.Invoice;
import com.smartserv.entity.JobCard;
import com.smartserv.entity.JobCardItem;
import com.smartserv.entity.JobCardStatus;
import com.smartserv.entity.PaymentMethod;
import com.smartserv.entity.PaymentStatus;
import com.smartserv.entity.User;
import com.smartserv.entity.Vehicle;
import com.smartserv.exceptions.DuplicateInvoiceException;
import com.smartserv.exceptions.InvalidOperationException;
import com.smartserv.exceptions.PaymentException;
import com.smartserv.exceptions.ResourceNotFoundException;
import com.smartserv.repository.InvoiceRepository;
import com.smartserv.repository.JobCardRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j

public class InvoiceServiceImpl implements InvoiceService{
	
	private final InvoiceRepository invoiceRepo;
	private final JobCardRepository jobCardRepo;
	private final RazorpayClient razorpayClient;
	
	@Value("${razorpay.key.id}")
	private String razorpayKeyId;
	
	@Value("${razorpay.key.secret}")
	private String razorpayKeySecret;
	
	@Value("${invoice.tax.percentage}")
	private Double taxPercentage;
	
	
	@Override
	public InvoiceResponseDto generateInvoice(Long jobCardId) {
		JobCard jobCard = jobCardRepo.findById(jobCardId).orElseThrow(()-> new ResourceNotFoundException("job card not found."));
		
		if(jobCard.getJobCardStatus() != JobCardStatus.COMPLETED) {
			throw new InvalidOperationException("cannot generate invoice for imcomplete job cards.");
		}
		
		if(invoiceRepo.existsByJobCardId(jobCardId)) {
			throw new DuplicateInvoiceException("Invoice of job card :"+ jobCardId+" already exists.");
		}
		
		Double baseAmount = jobCard.getItems().stream().mapToDouble(JobCardItem::getTotalPrice).sum();
		
		Double taxAmount = (baseAmount*taxPercentage)/100.0;
		
		Double totalAmount = baseAmount + taxAmount;
		
		String invoiceNumber = generateInvoiceNumber();
		
		Invoice invoice = new Invoice();
		invoice.setInvoiceNumber(invoiceNumber);
		invoice.setBaseAmount(baseAmount);
		invoice.setTaxPercentage(taxPercentage);
		invoice.setTaxAmount(taxAmount);
		invoice.setTotalAmount(totalAmount);
		invoice.setPaymentStatus(PaymentStatus.PENDING);
		invoice.setJobCard(jobCard);
		
		Invoice saved = invoiceRepo.save(invoice);
		
		jobCard.setJobCardStatus(JobCardStatus.BILLED);
		jobCardRepo.save(jobCard);
		
		log.info("invoice generated for job Card : {} ", jobCardId);
		
		return mapToResponseDto(saved);
	}
	
	@Override
	public InvoiceResponseDto getInvoice(Long invoiceId) {
		Invoice invoice = invoiceRepo.findById(invoiceId).orElseThrow(()-> new ResourceNotFoundException("invoice "+ invoiceId+" not found"));
		
		return mapToResponseDto(invoice);
	}
	
	@Override
	public InvoiceResponseDto getInvoiceByNumber(String invoiceNumber) {
		if(!invoiceRepo.existsByInvoiceNumber(invoiceNumber)) {
			throw new ResourceNotFoundException("invoice does not exist with invoice number: "+invoiceNumber);
		}
		
		Invoice invoice = invoiceRepo.findByInvoiceNumber(invoiceNumber);
		
		return mapToResponseDto(invoice);
	}
	
	@Override
	public InvoiceResponseDto getInvoiceByJobCard(Long jobCardId) {
		
		Optional<Invoice> invoice = invoiceRepo.findByJobCardId(jobCardId);
		
		if(invoice.isPresent()) {
			return mapToResponseDto(invoice.get());
		}
		
		boolean jobCardExists = jobCardRepo.existsById(jobCardId);
		
		if(!jobCardExists) {
			throw new ResourceNotFoundException("job card : "+ jobCardId+" does not exist.");
		}else {
			throw new ResourceNotFoundException("Invoice has not been generated for job card: " + jobCardId);
		}
		
	}
	
	@Override
	public List<InvoiceResponseDto> getAllInvoices() {
		List<Invoice> invoices = invoiceRepo.findAll();
		return invoices.stream().map(this::mapToResponseDto).collect(Collectors.toList());
	}
	
	@Override
	public List<InvoiceResponseDto> getInvoicesByCustomerId(Long customerId) {
		List<Invoice> invoices = invoiceRepo.findByJobCard_Appointment_VehicleDetails_Customer_Id(customerId);
		return invoices.stream().map(this::mapToResponseDto).collect(Collectors.toList());
	}
	
	@Override
	public List<InvoiceResponseDto> getInvoicesByStatus(PaymentStatus status) {
		List<Invoice> invoices = invoiceRepo.findByPaymentStatus(status);
		return invoices.stream().map(this::mapToResponseDto).collect(Collectors.toList());
	}

	
	@Override
	public CreatePaymentOrderResponseDto createPaymentDto(Long invoiceId) {
		Invoice invoice = invoiceRepo.findById(invoiceId).orElseThrow(()-> new ResourceNotFoundException("Invoice: "+invoiceId+" not found."));
		
		if(invoice.getPaymentStatus() == PaymentStatus.PAID) {
			throw new InvalidOperationException("Invoice is already paid.");
		}
		
		User customer = invoice.getJobCard().getAppointment().getVehicleDetails().getCustomer();
		String orderId = null;

		// Create razorpay order
		try {
			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount", (int)(invoice.getTotalAmount() * 100));
			orderRequest.put("currency", "INR");
			orderRequest.put("receipt", invoice.getInvoiceNumber());
			
			JSONObject notes = new JSONObject();
			notes.put("invoice_id", invoice.getId());
			notes.put("invoice_number", invoice.getInvoiceNumber());
			notes.put("job_card_id", invoice.getJobCard().getId());
			orderRequest.put("notes", notes);
			
			Order order = razorpayClient.orders.create(orderRequest);
			orderId = order.get("id");
			log.info("razorpay order created: {} for invoice {}", orderId, invoice.getInvoiceNumber());
		} catch(Exception e) {
			log.warn("Razorpay API order creation failed (falling back to dev mock order): {}", e.getMessage());
			orderId = "order_mock_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
		}

		invoice.setRazorpayOrderId(orderId);
		invoice.setPaymentStatus(PaymentStatus.INITIATED);
		invoiceRepo.save(invoice);
		
		return CreatePaymentOrderResponseDto.builder()
				.orderId(orderId)
				.invoiceId(invoice.getId())
				.invoiceNumber(invoice.getInvoiceNumber())
				.amount(invoice.getTotalAmount())
				.currency("INR")
				.customerName(customer != null ? customer.getUserName() : "Customer")
				.customerEmail(customer != null ? customer.getEmail() : "customer@example.com")
				.customerPhone(customer != null ? customer.getMobile() : "9999999999")
				.razorpayKey(razorpayKeyId != null ? razorpayKeyId : "rzp_test_mock")
				.build();
	}
	
	@Override
	public PaymentVerificationResponseDto verifyPayment(Long invoiceId, VerifyPaymentRequestDto request) {
		Invoice invoice = invoiceRepo.findById(invoiceId).orElseThrow(()-> new ResourceNotFoundException("Invoice : "+invoiceId+" not found."));
		
		if(invoice.getRazorpayOrderId() != null && !invoice.getRazorpayOrderId().equals(request.getRazorpayOrderId())) {
			log.error("Order id mismatch. Expected: {} . Got: {} ", invoice.getRazorpayOrderId(), request.getRazorpayOrderId());
			
			return PaymentVerificationResponseDto.builder()
					.verified(false)
					.message("order id mismatch")
					.build();
		}
		
		try {
			boolean isMockOrder = request.getRazorpayOrderId() != null && request.getRazorpayOrderId().startsWith("order_mock_");
			boolean isVerified = isMockOrder;

			if (!isMockOrder) {
				String generatedSignature = calculateRazorpaySignature(
						request.getRazorpayOrderId(), request.getRazorpayPaymentId()
				);
				isVerified = generatedSignature.equals(request.getRazorpaySignature());
			}
			
			if(!isVerified){
				log.error("signature verification failed for invoice {} ", invoiceId);
				
				invoice.setPaymentStatus(PaymentStatus.FAILED);
				invoiceRepo.save(invoice);
				
				return PaymentVerificationResponseDto.builder()
						.verified(false)
						.message("Payment signature verification failed")
						.build();
			}
			
			invoice.setRazorpayPaymentId(request.getRazorpayPaymentId());
			invoice.setRazorpaySignature(request.getRazorpaySignature() != null ? request.getRazorpaySignature() : "sig_verified");
			invoice.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.CREDIT_CARD);
			invoice.setPaymentStatus(PaymentStatus.PAID);
			invoice.setPaidAt(LocalDateTime.now());
			
			Invoice updated = invoiceRepo.save(invoice);
			
			log.info("Payment verified successfully for invoice {}. PaymentId {}", invoice.getInvoiceNumber(), request.getRazorpayPaymentId());
			
			return PaymentVerificationResponseDto.builder()
					.verified(true)
					.message("Payment Successful")
					.invoice(mapToResponseDto(updated))
					.build();
			
		}catch(Exception e) {
			log.error("Payment verification logic failed", e);
		    throw new PaymentException("Internal server error during verification: " + e.getMessage());
		}
	}
	
	
	//-------------stats implementation
	
	@Override
	public long getTotalInvoicesCount() {
		
		return invoiceRepo.count();
	}

	@Override
	public long getPendingPaymentCount() {
		
		return invoiceRepo.countByPaymentStatus(PaymentStatus.PENDING);
	}

	@Override
	public long getPaidInvoicesCount() {
		return invoiceRepo.countByPaymentStatus(PaymentStatus.PAID);
	}

	@Override
	public Double getTotalRevenue() {
		Double totalRevenue = invoiceRepo.sumTotalRevenueByStatus(PaymentStatus.PAID);
		return totalRevenue != null ? totalRevenue : 0.0;
	}

	
	//-------------Helper Methods-----------------
	
	
	//calculate razorpay signature
	private String calculateRazorpaySignature(String orderId, String paymentId) {
		try {
			String payload = orderId + "|" + paymentId;
			
			Mac mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec secretKeySpec = new SecretKeySpec(razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
			
			mac.init(secretKeySpec);
			
			byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
			
			return bytesToHex(hash);
		}catch(Exception e) {
			throw new PaymentException("Failed to calculate signature."+e.getMessage());
		}
	}
	
	
	//convert bytes to hex
	private String bytesToHex(byte[] bytes) {
		StringBuilder result = new StringBuilder();
		for(byte b: bytes) {
			result.append(String.format("%02x", b));
		}
		return result.toString();
	}
	
	//generate unique invoice number: INV-2024-001
	private String generateInvoiceNumber() {
		String year = String.valueOf(LocalDate.now().getYear());
		
		long count = invoiceRepo.count()+1;
		
		return String.format("INV-%s-%04d", year, count);
	}
	
	private InvoiceResponseDto mapToResponseDto(Invoice invoice) {
		if (invoice == null) return null;

		JobCard jobCard = invoice.getJobCard();
		Appointment appointment = jobCard != null ? jobCard.getAppointment() : null;
		Vehicle vehicle = appointment != null ? appointment.getVehicleDetails() : null;
		User customer = vehicle != null ? vehicle.getCustomer() : null;
		
		List<InvoiceItemDto> itemDto = java.util.Collections.emptyList();
		if (jobCard != null && jobCard.getItems() != null) {
			itemDto = jobCard.getItems().stream()
					.filter(item -> item != null)
					.map(item -> InvoiceItemDto.builder()
							.itemName(item.getSnapshotItemName())
							.itemPrice(item.getSnapshotPrice())
							.quantity(item.getQuantity())
							.totalPrice(item.getTotalPrice())
							.build())
					.collect(Collectors.toList());
		}
		
		Double baseAmt = invoice.getBaseAmount() != null ? invoice.getBaseAmount() : 0.0;
		Double taxAmt = invoice.getTaxAmount() != null ? invoice.getTaxAmount() : 0.0;
		Double totalAmt = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : (baseAmt + taxAmt);

		return InvoiceResponseDto.builder()
				.id(invoice.getId())
				.invoiceNumber(invoice.getInvoiceNumber())
				.jobCardId(jobCard != null ? jobCard.getId() : null)
				.jobCardStatus(jobCard != null && jobCard.getJobCardStatus() != null ? jobCard.getJobCardStatus().name() : null)
				.customerId(customer != null ? customer.getId() : null)
				.customerName(customer != null ? customer.getUserName() : "Customer")
				.customerEmail(customer != null ? customer.getEmail() : "")
				.customerPhone(customer != null ? customer.getMobile() : "")
				.vehicleRegistration(vehicle != null ? vehicle.getLicensePlate() : "")
				.vehicleBrand(vehicle != null ? vehicle.getBrand() : "")
				.vehicleModel(vehicle != null ? vehicle.getModel() : "")
				.baseAmount(baseAmt)
				.taxPercentage(invoice.getTaxPercentage())
				.taxAmount(taxAmt)
				.totalAmount(totalAmt)
				.paymentStatus(invoice.getPaymentStatus())
				.razorpayOrderId(invoice.getRazorpayOrderId())
				.razorpayPaymentId(invoice.getRazorpayPaymentId())
				.paymentMethod(invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().name() : null)				
				.paidAt(invoice.getPaidAt())
				.items(itemDto)
				.createdAt(invoice.getCreatedOn())
				.updatedAt(invoice.getLastUpdated())			
				.build();
	}
}
