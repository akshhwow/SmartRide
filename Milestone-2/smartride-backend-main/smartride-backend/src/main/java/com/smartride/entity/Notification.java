package com.smartride.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user receiving the notification
    @Column(nullable = false)
    private Long recipientId;

    // The user causing the notification (can be null if system)
    private Long senderId;

    // Notification type: 'BOOKING_CONFIRMED', 'RIDE_ACCEPTED', 'RIDE_STARTED', 'RIDE_ENDED', 'DRIVER_CANCELLED', 'PAYMENT_CONFIRMED'
    @Column(nullable = false)
    private String type;

    // Optional ride ID for deep linking or context
    private Long rideId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
    }
}
