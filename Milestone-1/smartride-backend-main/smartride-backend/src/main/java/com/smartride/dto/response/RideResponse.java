package com.smartride.dto.response;

import com.smartride.entity.Ride;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class RideResponse {

    private Long id;
    private Long driverId;
    private String driverName;
    private Double driverRating;
    private String driverPhone;
    private String driverVehicleType;
    private String driverCarModel;
    private String driverLicensePlate;

    private String source;
    private String destination;
    private LocalDate rideDate;
    private LocalTime departureTime;
    private Integer seatsOffered;
    private Integer availableSeats;
    private Double pricePerSeat;
    private String notes;
    private Double distanceKm;
    private Ride.RideStatus status;
    private LocalDateTime createdAt;
}