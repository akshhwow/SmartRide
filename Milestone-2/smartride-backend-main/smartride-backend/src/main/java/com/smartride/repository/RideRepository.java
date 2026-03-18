package com.smartride.repository;

import com.smartride.entity.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {

    // Find rides by driver
    List<Ride> findByDriverId(Long driverId);

    List<Ride> findByDriverIdOrderByRideDateDesc(Long driverId);

    // Search rides - all parameters
    List<Ride> findBySourceAndDestinationAndRideDateAndAvailableSeatsGreaterThanEqualAndStatus(
            String source, String destination, LocalDate rideDate, Integer seats, Ride.RideStatus status
    );

    // Search rides - without seats parameter
    List<Ride> findBySourceAndDestinationAndRideDateAndStatus(
            String source, String destination, LocalDate rideDate, Ride.RideStatus status
    );

    // Search rides - without date parameter
    List<Ride> findBySourceAndDestinationAndAvailableSeatsGreaterThanEqualAndStatus(
            String source, String destination, Integer seats, Ride.RideStatus status
    );

    // Search rides - only source and destination
    List<Ride> findBySourceAndDestinationAndStatus(
            String source, String destination, Ride.RideStatus status
    );

    // Find active rides by driver
    List<Ride> findByDriverIdAndStatus(Long driverId, Ride.RideStatus status);

    // Find rides by date range
    List<Ride> findByRideDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, Ride.RideStatus status);
}