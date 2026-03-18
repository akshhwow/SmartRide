package com.smartride.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * BookRideRequest - Data for POST /api/bookings  (Passenger books a ride)
 */
@Data
public class BookRideRequest {

    @NotNull(message = "Ride ID is required")
    private Long rideId;

    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "Must book at least 1 seat")
    private Integer seatsToBook;

    private String pickupLocation;  // Optional: specific pickup point
    private String dropLocation;    // Optional: specific drop point
    private String passengerNotes;  // Optional: any message to driver
}
