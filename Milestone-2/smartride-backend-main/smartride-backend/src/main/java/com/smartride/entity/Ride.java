package com.smartride.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * ============================================================
 * Ride Entity - Represents a ride posted by a driver
 * ============================================================
 *
 * This entity stores all information about a ride including:
 * - Driver details
 * - Route (source/destination)
 * - Schedule (date/time)
 * - Pricing and seats
 * - Status
 *
 * 📌 FIX APPLIED:
 *    Added 'totalSeats' field mapped to 'total_seats' column.
 *    The database table had this column but the entity was missing it.
 *    Error was: "Field 'total_seats' doesn't have a default value"
 *    Because the DB column is NOT NULL but we never set it on insert.
 *
 * 📌 Also added @Builder for cleaner object construction,
 *    and @PrePersist / @PreUpdate to auto-set timestamps.
 * ============================================================
 */
@Entity
@Table(name = "rides")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Links to the User (driver) who posted this ride
    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    // Where the ride starts
    @Column(nullable = false, length = 200)
    private String source;

    // Where the ride ends
    @Column(nullable = false, length = 200)
    private String destination;

    // Date of the ride e.g. 2026-03-15
    @Column(name = "ride_date", nullable = false)
    private LocalDate rideDate;

    // Time the ride departs e.g. 08:00
    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;

    // How many seats the driver is offering total
    // e.g. driver has 4-seat car, offers 3 seats to passengers
    @Column(name = "seats_offered", nullable = false)
    private Integer seatsOffered;

    // ✅ FIX: total_seats column was in DB but missing from entity
    // total_seats = same as seatsOffered (total capacity being shared)
    // We keep this in sync with seatsOffered at all times
    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    // How many seats are still available for booking
    // Starts equal to seatsOffered, decreases as passengers book
    // e.g. seatsOffered=3, 1 booked → availableSeats=2
    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    // Price per seat in rupees e.g. 500.00
    @Column(name = "price_per_seat", nullable = false)
    private Double pricePerSeat;

    // Base fare (fixed amount) for a ride, before distance calculation
    @Column(name = "base_fare")
    private Double baseFare;

    // Fare charged per kilometer
    @Column(name = "fare_per_km")
    private Double farePerKm;

    // Optional notes from driver e.g. "AC car, no smoking"
    @Column(length = 1000)
    private String notes;

    // Distance of the ride in kilometers e.g. 150.0
    @Column(name = "distance_km")
    private Double distanceKm;

    // Current status of the ride
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status = RideStatus.ACTIVE;

    // When the ride was posted
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // When the ride was last updated
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * ✅ Runs automatically BEFORE a new ride is saved to database
     * Sets createdAt and updatedAt timestamps
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        // ✅ FIX: Auto-set totalSeats = seatsOffered if not already set
        // This ensures total_seats is NEVER null on insert
        if (this.totalSeats == null && this.seatsOffered != null) {
            this.totalSeats = this.seatsOffered;
        }
    }

    /**
     * ✅ Runs automatically BEFORE an existing ride is updated in database
     * Updates the updatedAt timestamp
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Ride Status Enum - The possible states a ride can be in
     *
     * ACTIVE    = Ride is posted and available for booking
     * FULL      = All seats have been booked (no more bookings possible)
     * CANCELLED = Driver cancelled the ride
     * COMPLETED = Ride has been completed successfully
     */
    public enum RideStatus {
        ACTIVE,
        FULL,
        CANCELLED,
        COMPLETED
    }
}