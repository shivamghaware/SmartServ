package com.smartserv.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartserv.dto.invoice.PaymentVerificationResponseDto;
import com.smartserv.dto.invoice.VerifyPaymentRequestDto;
import com.smartserv.entity.PaymentStatus;
import com.smartserv.service.InvoiceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Slf4j
public class InvoiceController {
	private final InvoiceService invoiceService;
	
	//------------------Invoice Generation-------------------
	
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@PostMapping("/generate/job_card/{jobCardId}")
	public ResponseEntity<?> generateInvoice(@PathVariable Long jobCardId){
		log.info("generating invoice for job card : {} ", jobCardId);
		return ResponseEntity.ok(invoiceService.generateInvoice(jobCardId));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<?> getInvoiceById(@PathVariable Long id){
		return ResponseEntity.ok(invoiceService.getInvoice(id));
	}
	
	@GetMapping("/number/{invoiceNumber}")
	public ResponseEntity<?> getInvoiceByNumber(@PathVariable String invoiceNumber){
		return ResponseEntity.ok(invoiceService.getInvoiceByNumber(invoiceNumber));
	}
	
	@GetMapping("/job_card/{jobCardId}")
	public ResponseEntity<?> getInvoiceByJobCard(@PathVariable Long jobCardId){
		return ResponseEntity.ok(invoiceService.getInvoiceByJobCard(jobCardId));
	}
	
	
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping
	public ResponseEntity<?> getAllInvoices(){
		return ResponseEntity.ok(invoiceService.getAllInvoices());
	}
	
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or #customerId == authentication.credentials")
	@GetMapping("/customer/{customerId}")
	public ResponseEntity<?> getInvoicesByCustomerId(@PathVariable Long customerId){
		return ResponseEntity.ok(invoiceService.getInvoicesByCustomerId(customerId));
	}
	
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/status/{status}")
	public ResponseEntity<?> getInvoiceByStatus(@PathVariable PaymentStatus status){
		return ResponseEntity.ok(invoiceService.getInvoicesByStatus(status));
	}
	
	
	//--------------Payment Operations------------------------
	
	@PostMapping("/{id}/create_payment_order")
	public ResponseEntity<?> createPaymentOrder(@PathVariable Long id){
		return ResponseEntity.ok(invoiceService.createPaymentDto(id));
	}
	
	
	@PostMapping("/{id}/verify_payment")
	public ResponseEntity<?> verifyPayment(@PathVariable Long id, @Valid @RequestBody VerifyPaymentRequestDto request){
		
		PaymentVerificationResponseDto response = invoiceService.verifyPayment(id, request);
		
		if(response.getVerified()) {
			return ResponseEntity.ok(response);
		}else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}
	
	
	
	//-------------------Statistice-----------------------
	
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/stats/total_count")
	public ResponseEntity<?> getTotalInvoiceCount(){
		return ResponseEntity.ok(invoiceService.getTotalInvoicesCount());
	}
	
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/stats/pending_count")
	public ResponseEntity<?> getPendingPaymentCount(){
		return ResponseEntity.ok(invoiceService.getPendingPaymentCount());
	}
	
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/stats/paid_count")
	public ResponseEntity<?> getPaidInvoiceCount(){
		return ResponseEntity.ok(invoiceService.getPaidInvoicesCount());
	}
	
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	@GetMapping("/stats/total_revenue")
	public ResponseEntity<?> getTotalRevenue(){
		return ResponseEntity.ok(invoiceService.getTotalRevenue());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
