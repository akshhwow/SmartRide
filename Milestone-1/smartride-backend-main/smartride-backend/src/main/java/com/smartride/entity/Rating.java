package com.smartride.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false)
    private Long driverId;

    @Column(nullable = false)
    private Long passengerId;

    @Column(nullable = false)
    private Integer rating; // 1 to 5

    @Column(length = 500)
    private String review;

    @Enumerated(EnumType.STRING)
    private RatingType ratingType; // DRIVER_RATED_PASSENGER or PASSENGER_RATED_DRIVER

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum RatingType {
        PASSENGER_RATED_DRIVER,
        DRIVER_RATED_PASSENGER
    }
}