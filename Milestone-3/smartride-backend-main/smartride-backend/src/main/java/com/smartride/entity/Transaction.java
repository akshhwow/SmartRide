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
 * Transaction entity - tracks wallet movements for users (drivers/passengers).
 *
 * This can be used for:
 * - Driver earnings (credit)
 * - Passenger refunds (credit)
 * - Payment debits when booking a ride
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Owner of this transaction (usually a driver or passenger).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Optional link to a payment record that triggered this transaction.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    /**
     * Optional link to the booking associated with this transaction.
     */
    @Column(name = "booking_id")
    private Long bookingId;

    /**
     * Amount changed in this transaction (positive for credit, negative for debit).
     */
    @Column(nullable = false)
    private Double amount;

    /**
     * Balance of the wallet after this transaction.
     */
    @Column(name = "balance_after")
    private Double balanceAfter;

    /**
     * Type of transaction (DEBIT/CREDIT/REFUND).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    /**
     * Optional human-readable note (e.g., "Booking payment", "Driver payout").
     */
    @Column(length = 500)
    private String note;

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

    public enum TransactionType {
        DEBIT,
        CREDIT,
        REFUND
    }
}
