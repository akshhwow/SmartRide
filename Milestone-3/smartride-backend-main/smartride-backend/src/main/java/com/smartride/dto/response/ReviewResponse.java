package com.smartride.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Long reviewerId;
    private String reviewerName; // We will fetch this from the User entity
    private Long rideId;
    private String rideRoute;    // We will build this as "Source → Destination"
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
