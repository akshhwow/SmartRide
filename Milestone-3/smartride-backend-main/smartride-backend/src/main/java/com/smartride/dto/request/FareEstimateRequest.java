package com.smartride.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request payload for fare estimation.
 */
@Data
public class FareEstimateRequest {

    @NotNull(message = "Ride ID is required")
    private Long rideId;

    @NotNull(message = "Origin latitude is required")
    private Double originLat;

    @NotNull(message = "Origin longitude is required")
    private Double originLng;

    @NotNull(message = "Destination latitude is required")
    private Double destinationLat;

    @NotNull(message = "Destination longitude is required")
    private Double destinationLng;

    @NotNull(message = "Seats to book is required")
    @DecimalMin(value = "1", message = "Must book at least one seat")
    private Integer seatsBooked;
}
