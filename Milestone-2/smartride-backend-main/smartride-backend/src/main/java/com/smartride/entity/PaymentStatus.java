package com.smartride.entity;

/**
 * Shared enum used by Booking + Payment to represent the current payment state.
 */
public enum PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}
