package com.smartride.repository;

import com.smartride.entity.RideBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for RideBooking snapshots.
 */
@Repository
public interface RideBookingRepository extends JpaRepository<RideBooking, Long> {

    Optional<RideBooking> findByBookingId(Long bookingId);
}
