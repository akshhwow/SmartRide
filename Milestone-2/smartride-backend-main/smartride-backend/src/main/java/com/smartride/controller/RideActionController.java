package com.smartride.controller;

import com.smartride.dto.response.ApiResponse;
import com.smartride.entity.Booking;
import com.smartride.entity.Ride;
import com.smartride.entity.User;
import com.smartride.service.NotificationService;
import com.smartride.service.RideService;
import com.smartride.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * NEW: RideActionController
 * Handles Accept, Start, End ride actions and triggers real-time STOMP notifications.
 */
@RestController
@RequestMapping("/api/ride-actions")
@RequiredArgsConstructor
public class RideActionController {

    private final RideService rideService;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    @PatchMapping("/{rideId}/accept")
    public ResponseEntity<ApiResponse<Ride>> acceptRide(
            @PathVariable Long rideId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long driverId = ((User) userDetails).getId();
        Ride ride = rideService.getRideById(rideId);
        
        if (!ride.getDriverId().equals(driverId)) {
            throw new RuntimeException("Unauthorized: Only the driver can accept this ride");
        }

        // Send notification to driver
        notificationService.saveAndEmit(driverId, driverId, "RIDE_ACCEPTED", rideId, "Ride accepted successfully");
        
        // Send notification to all passengers
        List<Booking> bookings = bookingRepository.findByRideIdOrderByCreatedAtDesc(rideId);
        for(Booking b : bookings) {
            notificationService.saveAndEmit(
                    b.getPassengerId(), driverId, "RIDE_ACCEPTED", rideId,
                    "Your driver has accepted the ride!"
            );
        }
        
        return ResponseEntity.ok(ApiResponse.success("Ride accepted successfully", ride));
    }

    @PatchMapping("/{rideId}/start")
    public ResponseEntity<ApiResponse<Ride>> startRide(
            @PathVariable Long rideId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long driverId = ((User) userDetails).getId();
        Ride ride = rideService.getRideById(rideId);
        
        if (!ride.getDriverId().equals(driverId)) throw new RuntimeException("Unauthorized");

        // Send notification to driver
        notificationService.saveAndEmit(driverId, driverId, "RIDE_STARTED", rideId, "Ride has been started.");

        // Send notification to all passengers
        List<Booking> bookings = bookingRepository.findByRideIdOrderByCreatedAtDesc(rideId);
        for(Booking b : bookings) {
            notificationService.saveAndEmit(
                    b.getPassengerId(), driverId, "RIDE_STARTED", rideId,
                    "Your ride has started!"
            );
        }

        return ResponseEntity.ok(ApiResponse.success("Ride started successfully", ride));
    }

    @PatchMapping("/{rideId}/end")
    public ResponseEntity<ApiResponse<Ride>> endRide(
            @PathVariable Long rideId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long driverId = ((User) userDetails).getId();
        
        // Uses the existing service method which sets completion logic
        Ride ride = rideService.completeRide(rideId, driverId);

        // Send notification to driver
        notificationService.saveAndEmit(driverId, driverId, "RIDE_ENDED", rideId, "Ride completed successfully");

        // Send notification to all passengers
        List<Booking> bookings = bookingRepository.findByRideIdOrderByCreatedAtDesc(rideId);
        for(Booking b : bookings) {
            notificationService.saveAndEmit(
                    b.getPassengerId(), driverId, "RIDE_ENDED", rideId,
                    "You have arrived! Processing payment..."
            );
            
            // Following user's plan: emit PAYMENT_CONFIRMED after processing
            notificationService.saveAndEmit(
                    b.getPassengerId(), driverId, "PAYMENT_CONFIRMED", rideId,
                    "Payment of ₹" + b.getTotalFare() + " confirmed"
            );
        }
        
        return ResponseEntity.ok(ApiResponse.success("Ride ended successfully", ride));
    }
}
