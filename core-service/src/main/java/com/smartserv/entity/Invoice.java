package com.smartserv.entity;

import java.time.LocalDateTime;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="invoice")
@AttributeOverride(name="id", column=@Column(name="invoice_id"))
@Getter
@Setter

public class Invoice extends BaseEntity{

    @Column(name="invoice_number", nullable=false, unique = true)
    private String invoiceNumber;

    @Column(name="base_amount", nullable=false)
    private Double baseAmount;

    @Column(name="tax_percentage", nullable=false)
    private Double taxPercentage;

    @Column(name="tax_amount", nullable=false)
    private Double taxAmount;

    @Column(name="total_amount", nullable=false)
    private Double totalAmount;

    @Column(name="payment_status", nullable=false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name="razorpay_order_id")
    private String razorpayOrderId;

    @Column(name="razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(name="razorpay_signature")
    private String razorpaySignature;

    @Column(name="payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name="paid_at")
    private LocalDateTime paidAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="job_card_id", nullable=false, unique = true)
    private JobCard jobCard;
}

