package com.smartride.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================
 * Booking Entity - Represents a seat booking made by a passenger
 * ============================================================
 *
 * This entity stores:
 * - Which ride was booked (linked to Ride entity)
 * - Which passenger booked it (passengerId)
 * - How many seats were booked
 * - Total fare paid
 * - Pickup and drop locations within the route
 * - Booking status (CONFIRMED / CANCELLED / COMPLETED)
 *
 * 📌 FIX APPLIED:
 *    createdAt and updatedAt were being set inline with = LocalDateTime.now()
 *    This can cause issues with JPA because the value is set at class-load time,
 *    not at persist time. Moved to @PrePersist for correct behavior.
 * ============================================================
 */
@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The ride this booking is for.
     * FetchType.LAZY = don't load the Ride data until it's actually needed
     * (saves unnecessary DB queries)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    /**
     * The ID of the passenger who made this booking.
     * Links to the users table where role = PASSENGER.
     */
    @Column(name = "passenger_id", nullable = false)
    private Long passengerId;

    /**
     * Number of seats booked in this transaction.
     * e.g. passenger books 2 seats → seatsBooked = 2
     */
    @Column(name = "seats_booked", nullable = false)
    private Integer seatsBooked;

    /**
     * Total fare = pricePerSeat × seatsBooked
     * e.g. 500.00 per seat × 2 seats = 1000.00
     */
    @Column(name = "total_fare", nullable = false)
    private Double totalFare;

    /**
     * Where the passenger will be picked up within the route
     * e.g. "Dadar Station"
     */
    @Column(name = "pickup_location", length = 500)
    private String pickupLocation;

    /**
     * Where the passenger will be dropped off within the route
     * e.g. "Shivaji Nagar"
     */
    @Column(name = "drop_location", length = 500)
    private String dropLocation;

    /**
     * Optional notes from the passenger to the driver
     * e.g. "I have 2 bags, please wait 5 mins"
     */
    @Column(name = "passenger_notes", length = 1000)
    private String passengerNotes;

    /**
     * Current status of this booking:
     * CONFIRMED  = Active booking, passenger is going
     * CANCELLED  = Passenger or admin cancelled it
     * COMPLETED  = Ride happened, booking is done
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;

    /**
     * When this booking was created.
     * updatable = false means this column is NEVER changed after creation.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When this booking was last updated.
     * Updated automatically by @PreUpdate.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * ✅ FIX: Set timestamps in @PrePersist (before saving to DB)
     * This is more reliable than setting them inline with = LocalDateTime.now()
     * because @PrePersist runs at the exact moment of DB insertion.
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Automatically update the updatedAt timestamp before any DB update
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Booking Status Enum
     */
    public enum BookingStatus {
        CONFIRMED,   // Booking is active
        CANCELLED,   // Booking was cancelled (seats restored to ride)
        COMPLETED    // Ride completed, booking is done
    }
}