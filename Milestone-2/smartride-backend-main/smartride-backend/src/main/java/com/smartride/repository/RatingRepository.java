package com.smartride.repository;

import com.smartride.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByBookingIdAndRatingType(Long bookingId, Rating.RatingType ratingType);

    List<Rating> findByDriverId(Long driverId);

    List<Rating> findByPassengerId(Long passengerId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.driverId = :driverId AND r.ratingType = 'PASSENGER_RATED_DRIVER'")
    Double getDriverAverageRating(Long driverId);
}