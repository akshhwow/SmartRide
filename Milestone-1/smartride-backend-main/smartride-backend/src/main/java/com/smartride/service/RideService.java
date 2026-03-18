package com.smartride.service;

import com.smartride.dto.request.RideRequest;
import com.smartride.dto.response.RideResponse;
import com.smartride.entity.Ride;
import com.smartride.entity.User;
import com.smartride.repository.RideRepository;
import com.smartride.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================
 * RideService - Handles all ride-related business logic
 * ============================================================
 *
 * This service manages:
 * - Posting new rides (drivers only)
 * - Searching for available rides (public)
 * - Getting ride details
 * - Getting driver's posted rides
 * - Cancelling and completing rides
 * - Driver ride statistics
 *
 * 📌 FIX APPLIED:
 *    In postRide(), we now also set ride.setTotalSeats(request.getSeatsOffered())
 *    This was missing, causing: "Field 'total_seats' doesn't have a default value"
 * ============================================================
 */
@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;

    /**
     * ============================================================
     * POST A NEW RIDE — Driver only
     * ============================================================
     *
     * 📌 What happens here:
     *    1. Verify the user is a DRIVER (not a passenger)
     *    2. Validate the ride date is not in the past
     *    3. Build the Ride object with all details
     *    4. Save to database and return the saved ride
     *
     * 📌 FIX: We now set totalSeats = seatsOffered
     *    totalSeats = the total number of seats being shared (never changes)
     *    availableSeats = starts same as totalSeats, decreases as people book
     * ============================================================
     */
    @Transactional
    public Ride postRide(Long driverId, RideRequest request) {

        // Step 1: Verify the user exists and is a DRIVER
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + driverId));

        if (driver.getRole() != User.Role.DRIVER) {
            throw new RuntimeException("Only drivers can post rides. Your role is: " + driver.getRole());
        }

        // Step 2: Validate ride date is not in the past
        if (request.getRideDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Ride date cannot be in the past. Please choose a future date.");
        }

        // Step 3: Build the new Ride object
        Ride ride = new Ride();
        ride.setDriverId(driverId);
        ride.setSource(request.getSource());
        ride.setDestination(request.getDestination());
        ride.setRideDate(request.getRideDate());
        ride.setDepartureTime(request.getDepartureTime());
        ride.setSeatsOffered(request.getSeatsOffered());

        // ✅ FIX: Set totalSeats — this was the missing piece causing the DB error
        // totalSeats = the maximum seats ever available on this ride (never changes)
        ride.setTotalSeats(request.getSeatsOffered());

        // availableSeats starts equal to seatsOffered (all seats free initially)
        // As passengers book, availableSeats will decrease
        ride.setAvailableSeats(request.getSeatsOffered());

        ride.setPricePerSeat(request.getPricePerSeat());
        ride.setNotes(request.getNotes());
        ride.setDistanceKm(request.getDistanceKm());

        // New ride always starts as ACTIVE
        ride.setStatus(Ride.RideStatus.ACTIVE);

        // Step 4: Save to database and return
        return rideRepository.save(ride);
    }

    /**
     * ============================================================
     * SEARCH RIDES — Public, no login needed
     * ============================================================
     *
     * Passengers use this to find rides matching their route.
     * Supports flexible searching:
     *   - source + destination only
     *   - + date filter
     *   - + minimum seats required
     *   - all four combined
     *
     * Only returns ACTIVE rides (not cancelled or completed)
     * ============================================================
     */
    public List<RideResponse> searchRides(String source, String destination,
                                          LocalDate date, Integer seats) {
        List<Ride> rides;

        if (date != null && seats != null) {
            // Full search: source + destination + date + minimum seats
            rides = rideRepository.findBySourceAndDestinationAndRideDateAndAvailableSeatsGreaterThanEqualAndStatus(
                    source, destination, date, seats, Ride.RideStatus.ACTIVE
            );
        } else if (date != null) {
            // Search: source + destination + date
            rides = rideRepository.findBySourceAndDestinationAndRideDateAndStatus(
                    source, destination, date, Ride.RideStatus.ACTIVE
            );
        } else if (seats != null) {
            // Search: source + destination + minimum seats
            rides = rideRepository.findBySourceAndDestinationAndAvailableSeatsGreaterThanEqualAndStatus(
                    source, destination, seats, Ride.RideStatus.ACTIVE
            );
        } else {
            // Basic search: source + destination only
            rides = rideRepository.findBySourceAndDestinationAndStatus(
                    source, destination, Ride.RideStatus.ACTIVE
            );
        }

        return rides.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * ============================================================
     * GET ALL RIDES POSTED BY A DRIVER
     * ============================================================
     * Returns all rides for a driver sorted by date (newest first)
     * Used in the driver's dashboard to see their posted rides
     * ============================================================
     */
    public List<RideResponse> getDriverRides(Long driverId) {
        List<Ride> rides = rideRepository.findByDriverIdOrderByRideDateDesc(driverId);

        return rides.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * ============================================================
     * GET A SPECIFIC RIDE BY ID
     * ============================================================
     * Returns full ride details for a given ride ID
     * Used when a passenger clicks on a ride to see details
     * ============================================================
     */
    public Ride getRideById(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with ID: " + rideId));
    }

    /**
     * ============================================================
     * UPDATE RIDE STATUS — Internal use (called by BookingService etc.)
     * ============================================================
     */
    @Transactional
    public void updateRideStatus(Long rideId, Ride.RideStatus status) {
        Ride ride = getRideById(rideId);
        ride.setStatus(status);
        rideRepository.save(ride);
    }

    /**
     * ============================================================
     * CANCEL A RIDE — Driver only
     * ============================================================
     * Only the driver who posted the ride can cancel it.
     * Cannot cancel already cancelled or completed rides.
     * ============================================================
     */
    @Transactional
    public Ride cancelRide(Long rideId, Long driverId) {
        Ride ride = getRideById(rideId);

        // Verify this ride belongs to the requesting driver
        if (!ride.getDriverId().equals(driverId)) {
            throw new RuntimeException("You can only cancel your own rides");
        }

        if (ride.getStatus() == Ride.RideStatus.CANCELLED) {
            throw new RuntimeException("This ride is already cancelled");
        }

        if (ride.getStatus() == Ride.RideStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a completed ride");
        }

        ride.setStatus(Ride.RideStatus.CANCELLED);
        return rideRepository.save(ride);
    }

    /**
     * ============================================================
     * COMPLETE A RIDE — Driver only
     * ============================================================
     * Marks a ride as completed after it has been taken.
     * Only the driver who posted it can complete it.
     * ============================================================
     */
    @Transactional
    public Ride completeRide(Long rideId, Long driverId) {
        Ride ride = getRideById(rideId);

        // Verify this ride belongs to the requesting driver
        if (!ride.getDriverId().equals(driverId)) {
            throw new RuntimeException("You can only complete your own rides");
        }

        if (ride.getStatus() == Ride.RideStatus.CANCELLED) {
            throw new RuntimeException("Cannot complete a cancelled ride");
        }

        if (ride.getStatus() == Ride.RideStatus.COMPLETED) {
            throw new RuntimeException("This ride is already completed");
        }

        ride.setStatus(Ride.RideStatus.COMPLETED);
        return rideRepository.save(ride);
    }

    /**
     * ============================================================
     * GET DRIVER RIDE STATISTICS
     * ============================================================
     * Returns summary stats for a driver's dashboard:
     * - Total rides posted
     * - Completed rides
     * - Active rides
     * - Cancelled rides
     * - Total earnings (from completed rides only)
     * ============================================================
     */
    public RideStatistics getDriverStatistics(Long driverId) {
        List<Ride> allRides = rideRepository.findByDriverId(driverId);

        long totalRides = allRides.size();

        long completedRides = allRides.stream()
                .filter(r -> r.getStatus() == Ride.RideStatus.COMPLETED)
                .count();

        long activeRides = allRides.stream()
                .filter(r -> r.getStatus() == Ride.RideStatus.ACTIVE)
                .count();

        long cancelledRides = allRides.stream()
                .filter(r -> r.getStatus() == Ride.RideStatus.CANCELLED)
                .count();

        // Earnings = (seats booked) × pricePerSeat for all COMPLETED rides
        // seats booked = seatsOffered - availableSeats (seats that were taken)
        double totalEarnings = allRides.stream()
                .filter(r -> r.getStatus() == Ride.RideStatus.COMPLETED)
                .mapToDouble(r -> (r.getSeatsOffered() - r.getAvailableSeats()) * r.getPricePerSeat())
                .sum();

        return new RideStatistics(totalRides, completedRides, activeRides,
                cancelledRides, totalEarnings);
    }

    /**
     * ============================================================
     * CONVERT Ride entity → RideResponse DTO
     * ============================================================
     * We never send raw entities to the frontend.
     * This method shapes the data into a clean response format
     * and also attaches driver details (name, rating, vehicle info).
     * ============================================================
     */
    private RideResponse convertToResponse(Ride ride) {

        // Load the driver who posted this ride to get their details
        User driver = userRepository.findById(ride.getDriverId()).orElse(null);

        RideResponse response = new RideResponse();
        response.setId(ride.getId());
        response.setDriverId(ride.getDriverId());
        response.setSource(ride.getSource());
        response.setDestination(ride.getDestination());
        response.setRideDate(ride.getRideDate());
        response.setDepartureTime(ride.getDepartureTime());
        response.setSeatsOffered(ride.getSeatsOffered());
        response.setAvailableSeats(ride.getAvailableSeats());
        response.setPricePerSeat(ride.getPricePerSeat());
        response.setNotes(ride.getNotes());
        response.setDistanceKm(ride.getDistanceKm());
        response.setStatus(ride.getStatus());
        response.setCreatedAt(ride.getCreatedAt());

        // Attach driver info so the passenger can see who is driving
        if (driver != null) {
            response.setDriverName(driver.getFullName());
            response.setDriverRating(driver.getRating() != null ? driver.getRating() : 0.0);
            response.setDriverPhone(driver.getPhone());
            response.setDriverVehicleType(driver.getVehicleType());
            response.setDriverCarModel(driver.getCarModel());
            response.setDriverLicensePlate(driver.getLicensePlate());
        }

        return response;
    }

    /**
     * ============================================================
     * RideStatistics — Inner class to hold driver stats summary
     * ============================================================
     */
    public static class RideStatistics {
        private final long totalRides;
        private final long completedRides;
        private final long activeRides;
        private final long cancelledRides;
        private final double totalEarnings;

        public RideStatistics(long totalRides, long completedRides, long activeRides,
                              long cancelledRides, double totalEarnings) {
            this.totalRides = totalRides;
            this.completedRides = completedRides;
            this.activeRides = activeRides;
            this.cancelledRides = cancelledRides;
            this.totalEarnings = totalEarnings;
        }

        public long getTotalRides()       { return totalRides; }
        public long getCompletedRides()   { return completedRides; }
        public long getActiveRides()      { return activeRides; }
        public long getCancelledRides()   { return cancelledRides; }
        public double getTotalEarnings()  { return totalEarnings; }
    }
}