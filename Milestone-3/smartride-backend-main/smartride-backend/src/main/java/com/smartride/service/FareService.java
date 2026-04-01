package com.smartride.service;

import org.springframework.stereotype.Service;

import com.smartride.dto.response.FareEstimateResponse;

/**
 * FareService - Calculates fare based on distance and driver pricing.
 */
@Service
public class FareService {

    /**
     * Calculates fare breakdown for a ride.
     */
    public FareEstimateResponse calculateFare(
            double distanceKm,
            double baseFare,
            double farePerKm,
            int seatsBooked,
            int totalSeats,
            Double explicitPricePerSeat
    ) {
        if (totalSeats <= 0) {
            throw new IllegalArgumentException("totalSeats must be greater than zero");
        }

        double totalFare;
        double farePerSeat;

        if (farePerKm > 0 && distanceKm > 0) {
            totalFare = baseFare + (farePerKm * distanceKm);
            farePerSeat = totalFare / totalSeats;
        } else if (explicitPricePerSeat != null && explicitPricePerSeat > 0) {
            farePerSeat = explicitPricePerSeat;
            totalFare = farePerSeat * totalSeats;
        } else {
            totalFare = baseFare + (farePerKm * distanceKm);
            farePerSeat = totalFare / totalSeats;
        }

        double passengerFare = farePerSeat * seatsBooked;

        FareEstimateResponse response = new FareEstimateResponse();
        response.setDistanceKm(distanceKm);
        response.setBaseFare(baseFare);
        response.setFarePerKm(farePerKm);
        response.setTotalFare(totalFare);
        response.setFarePerSeat(farePerSeat);
        response.setPassengerFare(passengerFare);
        response.setSeatsBooked(seatsBooked);

        return response;
    }
}
