package com.smartride.service;

import com.smartride.dto.request.ReviewRequest;
import com.smartride.entity.Review;
import com.smartride.entity.Ride;
import com.smartride.entity.User;
import com.smartride.entity.Booking;
import com.smartride.repository.BookingRepository;
import com.smartride.repository.ReviewRepository;
import com.smartride.repository.RideRepository;
import com.smartride.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import com.smartride.dto.response.ReviewResponse;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RideRepository rideRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    @Transactional
    public Review createReview(Long reviewerId, ReviewRequest request) {
        if (reviewerId.equals(request.getRevieweeId())) {
            throw new RuntimeException("You cannot review yourself");
        }

        Ride ride = rideRepository.findById(request.getRideId())
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.getStatus() != Ride.RideStatus.COMPLETED) {
            throw new RuntimeException("You can only review completed rides");
        }

        if (reviewRepository.existsByReviewerIdAndRideId(reviewerId, request.getRideId())) {
            throw new RuntimeException("You have already reviewed this ride");
        }

        User reviewee = userRepository.findById(request.getRevieweeId())
                .orElseThrow(() -> new RuntimeException("User to review not found"));

        // Validate participation
        boolean isDriverReviewingPassenger = ride.getDriverId().equals(reviewerId) && bookingRepository.findByRideIdAndStatus(ride.getId(), Booking.BookingStatus.COMPLETED).stream().anyMatch(b -> b.getPassengerId().equals(reviewee.getId()));
        
        boolean isPassengerReviewingDriver = ride.getDriverId().equals(reviewee.getId()) && bookingRepository.findByRideIdAndStatus(ride.getId(), Booking.BookingStatus.COMPLETED).stream().anyMatch(b -> b.getPassengerId().equals(reviewerId));
        
        if (!isDriverReviewingPassenger && !isPassengerReviewingDriver) {
            throw new RuntimeException("Review is not permitted. Only ride participants can review each other.");
        }

        Review review = new Review();
        review.setReviewerId(reviewerId);
        review.setRevieweeId(request.getRevieweeId());
        review.setRideId(request.getRideId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);

        // Update User Rating
        int currentTotal = reviewee.getTotalRatings() != null ? reviewee.getTotalRatings() : 0;
        double currentAvg = reviewee.getRating() != null ? reviewee.getRating() : 0.0;

        double newAvg = ((currentAvg * currentTotal) + request.getRating()) / (currentTotal + 1);

        reviewee.setTotalRatings(currentTotal + 1);
        reviewee.setRating(newAvg);
        userRepository.save(reviewee);

        // Notify user about review
        notificationService.saveAndEmit(
                reviewee.getId(),
                reviewerId,
                "NEW_REVIEW",
                request.getRideId(),
                "You received a new " + request.getRating() + "-star review!"
        );

        return savedReview;
    }

    public List<ReviewResponse> getUserReviews(Long userId) {
        List<Review> reviews = reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(userId);
        return reviews.stream().map(review -> {
            ReviewResponse response = new ReviewResponse();
            response.setId(review.getId());
            response.setReviewerId(review.getReviewerId());
            response.setRideId(review.getRideId());
            response.setRating(review.getRating());
            response.setComment(review.getComment());
            response.setCreatedAt(review.getCreatedAt());

            userRepository.findById(review.getReviewerId()).ifPresent(u -> response.setReviewerName(u.getFullName()));
            rideRepository.findById(review.getRideId()).ifPresent(r -> response.setRideRoute(r.getSource() + " → " + r.getDestination()));

            return response;
        }).collect(Collectors.toList());
    }
}
