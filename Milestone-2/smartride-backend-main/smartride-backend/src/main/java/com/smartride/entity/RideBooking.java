package com.smartride.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * RideBooking entity - an optional denormalized table for ride+booking snapshots.
 *
 * This table can be used to store derived information like fare, distance, and
 * payment pointers without mutating the original Booking record.
 */
@Entity
@Table(name = "ride_bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Optional link back to the main booking record.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", unique = true)
    private Booking booking;

    /**
     * Cached fare amount (in local currency).
     */
    @Column(nullable = false)
    private Double fare;

    /**
     * Cached distance in km (for reporting / fare breakdown).
     */
    @Column(name = "distance_km")
    private Double distanceKm;

    /**
     * Optional external payment identifier for this booking snapshot.
     */
    @Column(name = "payment_id", length = 200)
    private String paymentId;

    /**
     * Optional payment status for this snapshot.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

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
}
