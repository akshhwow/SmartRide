package com.smartride.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartride.dto.request.BookingRequest;
import com.smartride.dto.response.ApiResponse;
import com.smartride.dto.response.BookingResponse;
import com.smartride.entity.Booking;
import com.smartride.entity.User;
import com.smartride.service.BookingService;

import lombok.RequiredArgsConstructor;

/**
 * ============================================================
 * BookingController - REST endpoints for booking management
 * ============================================================
 *
 * 📌 THIS FILE WAS MISSING — which caused your 500 error:
 *    "No static resource api/bookings"
 *    Spring couldn't find any controller handling POST /api/bookings
 *    so it tried to serve it as a static file → crashed → 500 error.
 *
 * 📌 Available Endpoints:
 *    POST   /api/bookings                    → Book a ride (Passenger)
 *    GET    /api/bookings/my-bookings        → Get logged-in passenger's bookings
 *    GET    /api/bookings/ride/{rideId}      → Get all bookings for a ride (Driver)
 *    GET    /api/bookings/{bookingId}        → Get specific booking details
 *    PUT    /api/bookings/{bookingId}/cancel → Cancel a booking (Passenger)
 *
 * 📌 All endpoints require JWT token in Authorization header:
 *    Authorization: Bearer eyJhbGc...
 * ============================================================
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * ============================================================
     * POST /api/bookings — Book a ride (Passenger only)
     * ============================================================
     *
     * 📌 How to use in Postman:
     *    Method: POST
     *    URL: http://localhost:8080/api/bookings
     *    Headers: Authorization: Bearer {YOUR_JWT_TOKEN}
     *    Body (raw JSON):
     *    {
     *      "rideId": 1,
     *      "seatsToBook": 2,
     *      "pickupLocation": "Dadar Station",
     *      "dropLocation": "Shivaji Nagar"
     *    }
     *
     * 📌 Expected Response:
     *    {
     *      "success": true,
     *      "message": "Booking confirmed! You've booked 2 seat(s). Total fare: ₹1000.0",
     *      "data": {
     *        "bookingId": 1,
     *        "rideId": 1,
     *        "seatsBooked": 2,
     *        "totalFare": 1000.0,
     *        "status": "CONFIRMED"
     *      }
     *    }
     * ============================================================
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> bookRide(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody BookingRequest request
    ) {
        // Get the logged-in passenger's ID from JWT token
        Long passengerId = ((User) userDetails).getId();

        // Call service to create the booking
        Booking booking = bookingService.bookRide(passengerId, request);

        // Build success message with booking details
        String message = String.format(
                "Booking created! Please complete payment to confirm. Seats: %d, Amount: ₹%.1f",
                booking.getSeatsBooked(),
                booking.getTotalFare()
        );

        // Convert entity to response DTO
        BookingResponse response = bookingService.convertBookingToResponse(booking);

        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * ============================================================
     * GET /api/bookings/my-bookings — Get passenger's own bookings
     * ============================================================
     *
     * 📌 How to use in Postman:
     *    Method: GET
     *    URL: http://localhost:8080/api/bookings/my-bookings
     *    Headers: Authorization: Bearer {PASSENGER_JWT_TOKEN}
     *
     * 📌 Returns list of all bookings made by the logged-in passenger
     * ============================================================
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long passengerId = ((User) userDetails).getId();
        List<BookingResponse> bookings = bookingService.getPassengerBookings(passengerId);

        String message = bookings.isEmpty()
                ? "You have no bookings yet"
                : "Found " + bookings.size() + " booking(s)";

        return ResponseEntity.ok(ApiResponse.success(message, bookings));
    }

    /**
     * ============================================================
     * GET /api/bookings/ride/{rideId} — Get all bookings for a ride
     * ============================================================
     *
     * 📌 This is for DRIVERS to see who has booked their ride.
     * 📌 How to use in Postman:
     *    Method: GET
     *    URL: http://localhost:8080/api/bookings/ride/1
     *    Headers: Authorization: Bearer {DRIVER_JWT_TOKEN}
     *
     * 📌 Only the driver who posted the ride can see its bookings.
     * ============================================================
     */
    @GetMapping("/ride/{rideId}")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getRideBookings(
            @PathVariable Long rideId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long driverId = ((User) userDetails).getId();
        List<BookingResponse> bookings = bookingService.getRideBookings(rideId, driverId);

        String message = bookings.isEmpty()
                ? "No bookings for this ride yet"
                : "Found " + bookings.size() + " passenger(s) booked";

        return ResponseEntity.ok(ApiResponse.success(message, bookings));
    }

    /**
     * ============================================================
     * GET /api/bookings/{bookingId} — Get specific booking details
     * ============================================================
     *
     * 📌 How to use in Postman:
     *    Method: GET
     *    URL: http://localhost:8080/api/bookings/1
     *    Headers: Authorization: Bearer {JWT_TOKEN}
     * ============================================================
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = ((User) userDetails).getId();
        Booking booking = bookingService.getBookingById(bookingId);

        // Security check: only the passenger or driver can view this booking
        Long rideDriverId = booking.getRide().getDriverId();
        if (!booking.getPassengerId().equals(userId) && !rideDriverId.equals(userId)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You don't have permission to view this booking"));
        }

        BookingResponse response = bookingService.convertBookingToResponse(booking);
        return ResponseEntity.ok(ApiResponse.success("Booking details retrieved", response));
    }

    /**
     * ============================================================
     * PUT /api/bookings/{bookingId}/cancel — Cancel a booking
     * ============================================================
     *
     * 📌 Passenger cancels their own booking.
     *    Seats are automatically restored to the ride.
     * 📌 How to use in Postman:
     *    Method: PUT
     *    URL: http://localhost:8080/api/bookings/1/cancel
     *    Headers: Authorization: Bearer {PASSENGER_JWT_TOKEN}
     * ============================================================
     */
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long passengerId = ((User) userDetails).getId();
        Booking booking = bookingService.cancelBooking(bookingId, passengerId);

        BookingResponse response = bookingService.convertBookingToResponse(booking);
        return ResponseEntity.ok(ApiResponse.success(
                "Booking cancelled successfully. Seats have been restored.", response));
    }
}