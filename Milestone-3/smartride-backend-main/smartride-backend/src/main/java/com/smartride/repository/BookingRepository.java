package com.smartride.repository;

import com.smartride.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Booking entity
 * Provides database operations for bookings
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Find all bookings by passenger ID, ordered by creation date (newest first)
     */
    List<Booking> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);

    /**
     * Find all bookings for a specific ride, ordered by creation date (newest first)
     */
    List<Booking> findByRideIdOrderByCreatedAtDesc(Long rideId);

    /**
     * Find bookings by ride ID and status
     */
    List<Booking> findByRideIdAndStatus(Long rideId, Booking.BookingStatus status);

    /**
     * Check if a passenger has already booked a specific ride
     */
    boolean existsByRideIdAndPassengerId(Long rideId, Long passengerId);

    /**
     * Find a specific booking by ride and passenger
     */
    Optional<Booking> findByRideIdAndPassengerId(Long rideId, Long passengerId);

    /**
     * Find all confirmed bookings for a passenger
     */
    List<Booking> findByPassengerIdAndStatus(Long passengerId, Booking.BookingStatus status);

    /**
     * Count total bookings for a ride
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.ride.id = :rideId AND b.status = 'CONFIRMED'")
    Long countConfirmedBookingsByRideId(@Param("rideId") Long rideId);

    /**
     * Calculate total seats booked for a ride
     */
    @Query("SELECT COALESCE(SUM(b.seatsBooked), 0) FROM Booking b WHERE b.ride.id = :rideId AND b.status = 'CONFIRMED'")
    Integer getTotalSeatsBookedByRideId(@Param("rideId") Long rideId);
}