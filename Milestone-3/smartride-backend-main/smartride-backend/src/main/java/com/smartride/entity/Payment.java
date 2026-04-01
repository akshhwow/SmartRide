package com.smartride.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payment entity - records a payment attempt for a booking.
 *
 * This is a lightweight reference table that can be expanded later
 * to include payment gateway metadata, receipts, refund tracking, etc.
 */
@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the booking this payment is for.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    /**
     * Razorpay order ID (created when we request a new order from Razorpay).
     */
    @Column(name = "razorpay_order_id", length = 200, unique = true)
    private String razorpayOrderId;

    /**
     * Razorpay payment ID (assigned after successful payment)
     */
    @Column(name = "payment_id", length = 200, unique = true)
    private String paymentId;

    /**
     * Amount charged for this payment (in local currency, e.g., INR)
     */
    @Column(nullable = false)
    private Double fare;

    /**
     * Distance (in km) for the underlying ride. Stored here for auditing/analytics.
     */
    @Column(name = "distance_km")
    private Double distanceKm;

    /**
     * Current status of the payment (pending/paid/failed/refunded)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Transaction state for the payment.
     */
    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REFUNDED
    }
}
