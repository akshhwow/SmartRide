package com.smartride.service;

import com.smartride.dto.response.FareEstimateResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FareServiceTest {

    private final FareService fareService = new FareService();

    @Test
    void calculateFare_zeroDistance_shouldReturnBaseFare() {
        FareEstimateResponse r = fareService.calculateFare(
                0.0,  // distanceKm
                50.0, // baseFare
                10.0, // farePerKm
                1,    // seatsBooked
                3     // totalSeats
        );

        assertEquals(50.0, r.getTotalFare(), 0.001);
        assertEquals(50.0 / 3.0, r.getPassengerFare(), 0.001);
    }

    @Test
    void calculateFare_shortDistance_shouldCalculateCorrectly() {
        FareEstimateResponse r = fareService.calculateFare(
                5.0,   // distanceKm
                20.0,  // baseFare
                5.0,   // farePerKm
                1,     // seatsBooked
                2      // totalSeats
        );

        // totalFare = 20 + (5 * 5) = 45
        // passengerFare = 45 / 2 = 22.5
        assertEquals(45.0, r.getTotalFare(), 0.001);
        assertEquals(22.5, r.getPassengerFare(), 0.001);
    }

    @Test
    void calculateFare_longDistance_shouldCalculateCorrectly() {
        FareEstimateResponse r = fareService.calculateFare(
                500.0, // distanceKm
                100.0, // baseFare
                2.0,   // farePerKm
                1,     // seatsBooked
                4      // totalSeats
        );

        // totalFare = 100 + (2 * 500) = 1100
        // passengerFare = 1100 / 4 = 275
        assertEquals(1100.0, r.getTotalFare(), 0.001);
        assertEquals(275.0, r.getPassengerFare(), 0.001);
    }

    @Test
    void calculateFare_multipleSeatsBooking_shouldScalePassengerFare() {
        FareEstimateResponse r = fareService.calculateFare(
                100.0, // distanceKm
                50.0,  // baseFare
                1.0,   // farePerKm
                3,     // seatsBooked
                5      // totalSeats
        );

        // totalFare = 50 + (1 * 100) = 150
        // seatFare = 150 / 5 = 30
        // passengerFare = 30 * 3 = 90
        assertEquals(150.0, r.getTotalFare(), 0.001);
        assertEquals(90.0, r.getPassengerFare(), 0.001);
    }

    @Test
    void calculateFare_singleSeatBooking_shouldEqualSeatFare() {
        FareEstimateResponse r = fareService.calculateFare(
                120.0, // distanceKm
                30.0,  // baseFare
                3.0,   // farePerKm
                1,     // seatsBooked
                3      // totalSeats
        );

        // totalFare = 30 + (3 * 120) = 390
        // passengerFare = 390 / 3 = 130
        assertEquals(390.0, r.getTotalFare(), 0.001);
        assertEquals(130.0, r.getPassengerFare(), 0.001);
    }
}
