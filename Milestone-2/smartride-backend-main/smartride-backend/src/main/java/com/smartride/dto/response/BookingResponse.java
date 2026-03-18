package com.smartride.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.smartride.entity.Booking;
import com.smartride.entity.PaymentStatus;

import lombok.Data;

@Data
public class BookingResponse {

    private Long bookingId;
    private Long rideId;
    private String source;
    private String destination;
    private LocalDate rideDate;
    private LocalTime departureTime;
    private Integer seatsBooked;
    private Double totalFare;
    private PaymentStatus paymentStatus;
    private String pickupLocation;
    private String dropLocation;
    private String passengerNotes;
    private Booking.BookingStatus status;
    private LocalDateTime createdAt;

    // Passenger details (for driver to see)
    private String passengerName;
    private String passengerPhone;

    // Driver details (for passenger to see)
    private String driverName;
    private String driverPhone;
}