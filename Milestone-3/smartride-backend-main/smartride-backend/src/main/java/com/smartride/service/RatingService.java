package com.smartride.service;

import com.smartride.dto.request.RatingRequest;
import com.smartride.entity.*;
import com.smartride.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Transactional
    public Rating rateDriver(Long userId, RatingRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getPassengerId().equals(userId)) {
            throw new RuntimeException("You can only rate your own bookings");
        }

        if (booking.getStatus() != Booking.BookingStatus.COMPLETED) {
            throw new RuntimeException("Can only rate completed rides");
        }

        // Check if already rated
        if (ratingRepository.findByBookingIdAndRatingType(
                booking.getId(),
                Rating.RatingType.PASSENGER_RATED_DRIVER
        ).isPresent()) {
            throw new RuntimeException("You have already rated this ride");
        }

        Rating rating = new Rating();
        rating.setBookingId(booking.getId());
        rating.setDriverId(booking.getRide().getDriverId());
        rating.setPassengerId(userId);
        rating.setRating(request.getRating());
        rating.setReview(request.getReview());
        rating.setRatingType(Rating.RatingType.PASSENGER_RATED_DRIVER);

        return ratingRepository.save(rating);
    }

    public Double getDriverRating(Long driverId) {
        Double avgRating = ratingRepository.getDriverAverageRating(driverId);
        return avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0;
    }

    public List<Rating> getDriverReviews(Long driverId) {
        return ratingRepository.findByDriverId(driverId);
    }
}