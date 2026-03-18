package com.smartride.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartride.dto.ApiResponse;
import com.smartride.dto.request.CreatePaymentOrderRequest;
import com.smartride.dto.request.VerifyPaymentRequest;
import com.smartride.dto.response.PaymentOrderResponse;
import com.smartride.service.PaymentService;

import lombok.RequiredArgsConstructor;

/**
 * PaymentController - Razorpay integration endpoints.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create a Razorpay order for the given booking.
     */
    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreatePaymentOrderRequest request
    ) throws Exception {
        Long userId = ((com.smartride.entity.User) userDetails).getId();
        PaymentOrderResponse response = paymentService.createOrder(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Order created", response));
    }

    /**
     * Verify Razorpay payment signature and update booking/payment status accordingly.
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody VerifyPaymentRequest request
    ) throws Exception {
        Long userId = ((com.smartride.entity.User) userDetails).getId();
        paymentService.verifyPayment(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Payment verified and booking confirmed"));
    }
}
