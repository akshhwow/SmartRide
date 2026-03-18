package com.smartride.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartride.entity.Payment;

/**
 * Repository for Payment entity (payment history & status).
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentId(String paymentId);

    Optional<Payment> findByRazorpayOrderId(String orderId);

    Optional<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByBookingIdAndRazorpayOrderId(Long bookingId, String orderId);
}
