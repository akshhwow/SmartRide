package com.smartride.controller;

import com.smartride.dto.ApiResponse;
import com.smartride.dto.request.FareEstimateRequest;
import com.smartride.dto.response.FareEstimateResponse;
import com.smartride.entity.Ride;
import com.smartride.service.DistanceService;
import com.smartride.service.FareService;
import com.smartride.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * FareController - endpoints for fare estimation and related calculations.
 */
@RestController
@RequestMapping("/api/fare")
@RequiredArgsConstructor
public class FareController {

    private final RideService rideService;
    private final DistanceService distanceService;
    private final FareService fareService;

    /**
     * Estimate fare based on ride pricing and calculated distance.
     */
    @PostMapping("/estimate")
    public ResponseEntity<ApiResponse<FareEstimateResponse>> estimateFare(
            @Valid @RequestBody FareEstimateRequest request
    ) {
        Ride ride = rideService.getRideById(request.getRideId());

        double distanceKm = distanceService.getDistanceInKm(
                request.getOriginLat(),
                request.getOriginLng(),
                request.getDestinationLat(),
                request.getDestinationLng()
        );

        double baseFare = ride.getBaseFare() != null ? ride.getBaseFare() : 0.0;
        double farePerKm = ride.getFarePerKm() != null ? ride.getFarePerKm() : 0.0;
        int totalSeats = ride.getTotalSeats() != null ? ride.getTotalSeats() : 1;

        FareEstimateResponse response = fareService.calculateFare(
                distanceKm,
                baseFare,
                farePerKm,
                request.getSeatsBooked(),
                totalSeats
        );

        return ResponseEntity.ok(ApiResponse.success("Fare estimate calculated", response));
    }
}
