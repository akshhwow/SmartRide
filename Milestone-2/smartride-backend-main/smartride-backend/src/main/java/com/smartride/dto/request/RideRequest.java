package com.smartride.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class RideRequest {

    @NotBlank(message = "Source is required")
    @Size(max = 200, message = "Source must be less than 200 characters")
    private String source;

    @NotBlank(message = "Destination is required")
    @Size(max = 200, message = "Destination must be less than 200 characters")
    private String destination;

    @NotNull(message = "Ride date is required")
    @Future(message = "Ride date must be in the future")
    private LocalDate rideDate;

    @NotNull(message = "Departure time is required")
    private LocalTime departureTime;

    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "Must offer at least 1 seat")
    @Max(value = 8, message = "Cannot offer more than 8 seats")
    private Integer seatsOffered;

    @NotNull(message = "Base fare is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Base fare must be at least 0")
    private Double baseFare;

    @NotNull(message = "Fare per km is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Fare per km must be at least 0")
    private Double farePerKm;

    @NotNull(message = "Price per seat is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double pricePerSeat;

    @Size(max = 1000, message = "Notes must be less than 1000 characters")
    private String notes;

    @Min(value = 1, message = "Distance must be at least 1 km")
    private Double distanceKm;
}