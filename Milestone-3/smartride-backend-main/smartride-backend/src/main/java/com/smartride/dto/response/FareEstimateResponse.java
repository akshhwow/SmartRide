package com.smartride.dto.response;

import lombok.Data;

/**
 * Response returned by the fare estimation endpoint.
 */
@Data
public class FareEstimateResponse {

    private Double distanceKm;
    private Double baseFare;
    private Double farePerKm;
    private Double totalFare;

    /**
     * Total fare allocated to the passenger(s) booking this ride.
     * This already accounts for the number of seats booked.
     */
    private Double passengerFare;

    /**
     * Fare per seat calculated based on the ride configuration.
     * This is the amount shown to the passenger in the UI.
     */
    private Double farePerSeat;

    private Integer seatsBooked;
}
