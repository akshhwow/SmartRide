package com.smartride.controller;

import com.smartride.dto.ApiResponse;
import com.smartride.dto.request.RatingRequest;
import com.smartride.entity.Rating;
import com.smartride.entity.User;
import com.smartride.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/rate-driver")
    public ResponseEntity<ApiResponse<Rating>> rateDriver(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RatingRequest request
    ) {
        Long userId = ((User) userDetails).getId();
        Rating rating = ratingService.rateDriver(userId, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Rating submitted successfully!",
                rating
        ));
    }

    @GetMapping("/driver/{driverId}/average")
    public ResponseEntity<ApiResponse<Double>> getDriverRating(@PathVariable Long driverId) {
        Double rating = ratingService.getDriverRating(driverId);
        return ResponseEntity.ok(ApiResponse.success(
                "Driver rating retrieved",
                rating
        ));
    }

    @GetMapping("/driver/{driverId}/reviews")
    public ResponseEntity<ApiResponse<List<Rating>>> getDriverReviews(@PathVariable Long driverId) {
        List<Rating> reviews = ratingService.getDriverReviews(driverId);
        return ResponseEntity.ok(ApiResponse.success(
                "Reviews retrieved",
                reviews
        ));
    }
}