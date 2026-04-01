package com.smartride.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookingRequest {

    @NotNull(message = "Ride ID is required")
    private Long rideId;

    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "Must book at least 1 seat")
    @Max(value = 8, message = "Cannot book more than 8 seats")
    private Integer seatsToBook;

    @Size(max = 500, message = "Pickup location must be less than 500 characters")
    private String pickupLocation;

    @Size(max = 500, message = "Drop location must be less than 500 characters")
    private String dropLocation;

    @Size(max = 1000, message = "Notes must be less than 1000 characters")
    private String passengerNotes;

    // Optional coordinates for the passenger's requested pickup/dropoff.
    // These are used to calculate fare/distance when booking.
    private Double originLat;
    private Double originLng;
    private Double destinationLat;
    private Double destinationLng;
}