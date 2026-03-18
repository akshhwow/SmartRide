package com.smartride.controller;

import com.smartride.dto.response.ApiResponse;
import com.smartride.dto.request.RideRequest;
import com.smartride.dto.response.RideResponse;
import com.smartride.entity.Ride;
import com.smartride.entity.User;
import com.smartride.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * ============================================================
 * RideController - REST endpoints for ride management
 * ============================================================
 *
 * 📌 Available Endpoints:
 *    POST   /api/rides                  → Post a new ride (Driver only)
 *    GET    /api/rides/search           → Search rides (Public)
 *    GET    /api/rides/my-rides         → Driver's posted rides
 *    GET    /api/rides/statistics       → Driver statistics
 *    GET    /api/rides/{id}             → Get ride by ID
 *    PUT    /api/rides/{id}/cancel      → Cancel a ride (Driver only)
 *    PUT    /api/rides/{id}/complete    → Complete a ride (Driver only)
 *
 * 📌 FIX APPLIED:
 *    Import changed from:  com.smartride.dto.ApiResponse       ❌
 *    Import changed to:    com.smartride.dto.response.ApiResponse ✅
 *    The ApiResponse class lives in the 'response' sub-package.
 *    Wrong import = compilation error / class not found at runtime.
 * ============================================================
 */
@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    /**
     * ============================================================
     * POST /api/rides — Post a new ride (Driver only)
     * ============================================================
     *
     * 📌 How to use in Postman:
     *    Method: POST
     *    URL: http://localhost:8080/api/rides
     *    Headers: Authorization: Bearer {DRIVER_JWT_TOKEN}
     *    Body (raw JSON):
     *    {
     *      "source": "Mumbai",
     *      "destination": "Pune",
     *      "rideDate": "2026-03-15",
     *      "departureTime": "08:00",
     *      "seatsOffered": 3,
     *      "pricePerSeat": 500.00,
     *      "notes": "Comfortable sedan, AC, no smoking",
     *      "distanceKm": 150
     *    }
     * ============================================================
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Ride>> postRide(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RideRequest request
    ) {
        // Extract the logged-in driver's ID from JWT token
        Long driverId = ((User) userDetails).getId();

        // Create and save the ride
        Ride ride = rideService.postRide(driverId, request);

        return ResponseEntity.ok(ApiResponse.success(
                "Ride posted successfully! 🚗",
                ride
        ));
    }

    /**
     * ============================================================
     * GET /api/rides/search — Search for available rides (Public)
     * ============================================================
     *
     * 📌 No JWT token needed — anyone can search rides
     * 📌 How to use in Postman:
     *    Method: GET
     *    URL: http://localhost:8080/api/rides/search?source=Mumbai&destination=Pune&date=2026-03-15&seats=2
     *
     *    Required: source, destination
     *    Optional: date (YYYY-MM-DD), seats (minimum seats needed)
     * ============================================================
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<RideResponse>>> searchRides(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer seats
    ) {
        List<RideResponse> rides = rideService.searchRides(source, destination, date, seats);

        String message = rides.isEmpty()
                ? "No rides found matching your search"
                : "Found " + rides.size() + " ride(s)";

        return ResponseEntity.ok(ApiResponse.success(message, rides));
    }

    /**
     * ============================================================
     * GET /api/rides/my-rides — Get all rides posted by logged-in driver
     * ============================================================
     *
     * 📌 How to use in Postman:
     *    Method: GET
     *    URL: http://localhost:8080/api/rides/my-rides
     *    Headers: Authorization: Bearer {DRIVER_JWT_TOKEN}
     * ============================================================
     */
    @GetMapping("/my-rides")
    public ResponseEntity<ApiResponse<List<RideResponse>>> getMyRides(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long driverId = ((User) userDetails).getId();
        List<RideResponse> rides = rideService.getDriverRides(driverId);

        return ResponseEntity.ok(ApiResponse.success(
                "Retrieved " + rides.size() + " ride(s)",
                rides
        ));
    }

    /**
     * ============================================================
     * GET /api/rides/statistics — Get driver statistics
     * ============================================================
     *
     * 📌 Returns: total rides, completed, active, cancelled, earnings
     * 📌 How to use in Postman:
     *    Method: GET
     *    URL: http://localhost:8080/api/rides/statistics
     *    Headers: Authorization: Bearer {DRIVER_JWT_TOKEN}
     *
     * ⚠️ NOTE: This endpoint is declared BEFORE /{id} to prevent
     *    Spring from trying to treat "statistics" as a path variable ID.
     * ============================================================
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<RideService.RideStatistics>> getStatistics(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long driverId = ((User) userDetails).getId();
        RideService.RideStatistics stats = rideService.getDriverStatistics(driverId);

        return ResponseEntity.ok(ApiResponse.success(
                "Statistics retrieved",
                stats
        ));
    }

    /**
     * ============================================================
     * GET /api/rides/{id} — Get ride details by ID
     * ============================================================
     *
     * 📌 How to use in Postman:
     *    Method: GET
     *    URL: http://localhost:8080/api/rides/1
     *    Headers: Authorization: Bearer {JWT_TOKEN}
     * ============================================================
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Ride>> getRideById(@PathVariable Long id) {
        Ride ride = rideService.getRideById(id);
        return ResponseEntity.ok(ApiResponse.success("Ride details retrieved", ride));
    }

    /**
     * ============================================================
     * PUT /api/rides/{id}/cancel — Cancel a ride (Driver only)
     * ============================================================
     *
     * 📌 Only the driver who posted the ride can cancel it.
     * 📌 How to use in Postman:
     *    Method: PUT
     *    URL: http://localhost:8080/api/rides/1/cancel
     *    Headers: Authorization: Bearer {DRIVER_JWT_TOKEN}
     * ============================================================
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Ride>> cancelRide(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long driverId = ((User) userDetails).getId();
        Ride ride = rideService.cancelRide(id, driverId);

        return ResponseEntity.ok(ApiResponse.success(
                "Ride cancelled successfully",
                ride
        ));
    }

    /**
     * ============================================================
     * PUT /api/rides/{id}/complete — Mark ride as completed (Driver only)
     * ============================================================
     *
     * 📌 Only the driver who posted the ride can complete it.
     * 📌 How to use in Postman:
     *    Method: PUT
     *    URL: http://localhost:8080/api/rides/1/complete
     *    Headers: Authorization: Bearer {DRIVER_JWT_TOKEN}
     * ============================================================
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Ride>> completeRide(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long driverId = ((User) userDetails).getId();
        Ride ride = rideService.completeRide(id, driverId);

        return ResponseEntity.ok(ApiResponse.success(
                "Ride marked as completed",
                ride
        ));
    }
}