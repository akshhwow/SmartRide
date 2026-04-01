package com.smartride.controller;

import com.smartride.dto.response.ApiResponse;
import com.smartride.dto.response.ReviewResponse;
import com.smartride.dto.request.ReviewRequest;
import com.smartride.entity.Review;
import com.smartride.entity.User;
import com.smartride.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<Review>> addReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReviewRequest request
    ) {
        Long reviewerId = ((User) userDetails).getId();
        Review review = reviewService.createReview(reviewerId, request);
        return ResponseEntity.ok(ApiResponse.success("Review added successfully", review));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getUserReviews(@PathVariable Long userId) {
        List<ReviewResponse> reviews = reviewService.getUserReviews(userId);
        return ResponseEntity.ok(ApiResponse.success("User reviews retrieved", reviews));
    }
}
