package com.smartride.service;

import com.smartride.dto.request.BookingRequest;
import com.smartride.dto.response.BookingResponse;
import com.smartride.entity.Booking;
import com.smartride.entity.Ride;
import com.smartride.entity.User;
import com.smartride.repository.BookingRepository;
import com.smartride.repository.RideRepository;
import com.smartride.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================
 * BookingService - Handles all booking-related operations
 * ============================================================
 *
 * This service manages:
 * - Creating new bookings (passengers booking rides)
 * - Retrieving bookings for passengers
 * - Retrieving bookings for drivers (who booked their rides)
 * - Cancelling bookings (restores seats automatically)
 *
 * 📌 FIX APPLIED:
 *    convertToResponse() was private — BookingController couldn't call it.
 *    Changed to public convertBookingToResponse() so the controller
 *    can convert the saved Booking entity into a response DTO.
 * ============================================================
 */
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RideRepository rideRepository;
    private final UserRepository userRepository;

    /**
     * ============================================================
     * BOOK A RIDE — Passenger only
     * ============================================================
     *
     * Steps:
     *  1. Find the ride by ID
     *  2. Verify ride is ACTIVE (not cancelled/completed/full)
     *  3. Prevent duplicate bookings (same passenger booking same ride twice)
     *  4. Check enough seats are available
     *  5. Calculate total fare = pricePerSeat × seatsToBook
     *  6. Create and save the Booking
     *  7. Reduce available seats on the ride
     *  8. If 0 seats left, mark ride as FULL
     *
     * @Transactional ensures seats are updated atomically —
     * if anything fails, the whole operation rolls back.
     * ============================================================
     */
    @Transactional
    public Booking bookRide(Long userId, BookingRequest request) {

        // Step 1: Find the ride
        Ride ride = rideRepository.findById(request.getRideId())
                .orElseThrow(() -> new RuntimeException(
                        "Ride not found with ID: " + request.getRideId()));

        // Step 2: Verify ride is ACTIVE
        if (ride.getStatus() != Ride.RideStatus.ACTIVE) {
            throw new RuntimeException(
                    "This ride is not available for booking. Current status: " + ride.getStatus());
        }

        // Step 3: Prevent duplicate bookings — passenger can't book same ride twice
        if (bookingRepository.existsByRideIdAndPassengerId(request.getRideId(), userId)) {
            throw new RuntimeException("You have already booked this ride");
        }

        // Step 4: Check enough seats are available
        if (ride.getAvailableSeats() < request.getSeatsToBook()) {
            throw new RuntimeException(
                    "Not enough seats available. Only " + ride.getAvailableSeats() + " seat(s) left.");
        }

        // Step 5: Calculate total fare
        Double totalFare = ride.getPricePerSeat() * request.getSeatsToBook();

        // Step 6: Create the Booking object
        Booking booking = new Booking();
        booking.setRide(ride);
        booking.setPassengerId(userId);
        booking.setSeatsBooked(request.getSeatsToBook());
        booking.setTotalFare(totalFare);
        booking.setPickupLocation(request.getPickupLocation());
        booking.setDropLocation(request.getDropLocation());
        booking.setPassengerNotes(request.getPassengerNotes());
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        // Step 7: Reduce available seats on the ride
        ride.setAvailableSeats(ride.getAvailableSeats() - request.getSeatsToBook());

        // Step 8: If no seats left, mark ride as FULL
        if (ride.getAvailableSeats() == 0) {
            ride.setStatus(Ride.RideStatus.FULL);
        }

        // Save both ride (updated seats) and booking (new record)
        rideRepository.save(ride);
        return bookingRepository.save(booking);
    }

    /**
     * ============================================================
     * GET ALL BOOKINGS FOR A PASSENGER
     * ============================================================
     * Returns all bookings made by the passenger, newest first.
     * Used in the passenger's "My Bookings" dashboard.
     * ============================================================
     */
    public List<BookingResponse> getPassengerBookings(Long passengerId) {
        List<Booking> bookings = bookingRepository
                .findByPassengerIdOrderByCreatedAtDesc(passengerId);

        return bookings.stream()
                .map(this::convertBookingToResponse)
                .collect(Collectors.toList());
    }

    /**
     * ============================================================
     * GET ALL BOOKINGS FOR A RIDE — Driver only
     * ============================================================
     * Shows the driver who has booked their ride.
     * Only the driver who posted the ride can see its bookings.
     * ============================================================
     */
    public List<BookingResponse> getRideBookings(Long rideId, Long driverId) {

        // Verify the ride exists and belongs to this driver
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with ID: " + rideId));

        if (!ride.getDriverId().equals(driverId)) {
            throw new RuntimeException("You can only view bookings for your own rides");
        }

        List<Booking> bookings = bookingRepository.findByRideIdOrderByCreatedAtDesc(rideId);

        return bookings.stream()
                .map(this::convertBookingToResponse)
                .collect(Collectors.toList());
    }

    /**
     * ============================================================
     * CANCEL A BOOKING — Passenger only
     * ============================================================
     * Cancels the booking and automatically restores seats to the ride.
     * If the ride was FULL, it becomes ACTIVE again.
     * ============================================================
     */
    @Transactional
    public Booking cancelBooking(Long bookingId, Long userId) {

        // Find booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException(
                        "Booking not found with ID: " + bookingId));

        // Verify booking belongs to this passenger
        if (!booking.getPassengerId().equals(userId)) {
            throw new RuntimeException("You can only cancel your own bookings");
        }

        // Can't cancel already cancelled booking
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("This booking is already cancelled");
        }

        // Can't cancel completed booking
        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a completed booking");
        }

        // Mark booking as cancelled
        booking.setStatus(Booking.BookingStatus.CANCELLED);

        // Restore seats to the ride
        Ride ride = booking.getRide();
        ride.setAvailableSeats(ride.getAvailableSeats() + booking.getSeatsBooked());

        // If ride was FULL before, make it ACTIVE again (seats are now available)
        if (ride.getStatus() == Ride.RideStatus.FULL) {
            ride.setStatus(Ride.RideStatus.ACTIVE);
        }

        // Save both
        rideRepository.save(ride);
        return bookingRepository.save(booking);
    }

    /**
     * ============================================================
     * GET A SPECIFIC BOOKING BY ID
     * ============================================================
     */
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException(
                        "Booking not found with ID: " + bookingId));
    }

    /**
     * ============================================================
     * CONVERT Booking entity → BookingResponse DTO
     * ============================================================
     *
     * 📌 FIX: Changed from private to PUBLIC so BookingController
     *    can call this method directly after bookRide() returns
     *    a Booking entity that needs to be converted for the response.
     *
     * We never send raw entities to the frontend.
     * This shapes the booking data into a clean JSON response
     * and also attaches passenger and driver name/phone details.
     * ============================================================
     */
    public BookingResponse convertBookingToResponse(Booking booking) {
        Ride ride = booking.getRide();

        // Load passenger details to include in response
        User passenger = userRepository.findById(booking.getPassengerId()).orElse(null);

        // Load driver details to include in response
        User driver = userRepository.findById(ride.getDriverId()).orElse(null);

        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getId());
        response.setRideId(ride.getId());
        response.setSource(ride.getSource());
        response.setDestination(ride.getDestination());
        response.setRideDate(ride.getRideDate());
        response.setDepartureTime(ride.getDepartureTime());
        response.setSeatsBooked(booking.getSeatsBooked());
        response.setTotalFare(booking.getTotalFare());
        response.setPickupLocation(booking.getPickupLocation());
        response.setDropLocation(booking.getDropLocation());
        response.setPassengerNotes(booking.getPassengerNotes());
        response.setStatus(booking.getStatus());
        response.setCreatedAt(booking.getCreatedAt());

        if (passenger != null) {
            response.setPassengerName(passenger.getFullName());
            response.setPassengerPhone(passenger.getPhone());
        }

        if (driver != null) {
            response.setDriverName(driver.getFullName());
            response.setDriverPhone(driver.getPhone());
        }

        return response;
    }
}