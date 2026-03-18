package com.smartride.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartride.dto.request.CreatePaymentOrderRequest;
import com.smartride.dto.request.VerifyPaymentRequest;
import com.smartride.dto.response.PaymentOrderResponse;
import com.smartride.entity.Booking;
import com.smartride.entity.Payment;
import com.smartride.entity.PaymentStatus;
import com.smartride.entity.Transaction;
import com.smartride.entity.User;
import com.smartride.repository.BookingRepository;
import com.smartride.repository.PaymentRepository;
import com.smartride.repository.TransactionRepository;
import com.smartride.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * PaymentService - handles Razorpay order creation and verification.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${razorpay.currency:INR}")
    private String currency;

    @Value("${razorpay.key-id:}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret:}")
    private String razorpaySecret;

    @Value("${razorpay.api-url:https://api.razorpay.com/v1}")
    private String razorpayApiUrl;

    @Value("${app.payment.driver-share:0.7}")
    private double driverSharePercent;

    @PostConstruct
    public void validateRazorpayConfig() {
        if (razorpayKeyId == null || razorpayKeyId.isBlank() || razorpaySecret == null || razorpaySecret.isBlank()) {
            log.warn("Razorpay keyId/secret are not configured. Razorpay payment endpoints will fail until you set razorpay.key-id and razorpay.key-secret in application.properties.");
        }
    }

    /**
     * Create Razorpay order for a booking and return order details to frontend.
     */
    @Transactional
    public PaymentOrderResponse createOrder(Long userId, CreatePaymentOrderRequest request) throws Exception {
        log.info("Creating payment for bookingId: {} by userId: {}", request.getBookingId(), userId);

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getPassengerId().equals(userId)) {
            throw new RuntimeException("You can only create an order for your own booking");
        }

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Booking is already paid");
        }

        if (booking.getStatus() != Booking.BookingStatus.PENDING && booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new RuntimeException("Cannot create payment for booking in status: " + booking.getStatus());
        }

        // Ensure amount matches what we expect for this booking
        if (booking.getTotalFare() == null || request.getAmount() == null) {
            throw new RuntimeException("Booking amount or request amount is missing");
        }

        double expectedAmount = booking.getTotalFare();
        double requestedAmount = request.getAmount();
        if (Math.abs(expectedAmount - requestedAmount) > 0.01) {
            throw new RuntimeException("Amount mismatch for booking");
        }

        // If a payment already exists for this booking, reuse the existing Razorpay order.
        Optional<Payment> existingPayment = paymentRepository.findByBookingId(booking.getId());
        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            log.info("Found existing payment for bookingId: {} (orderId={})", booking.getId(), payment.getRazorpayOrderId());

            // If the existing payment is already marked PAID, do not allow new orders
            if (payment.getPaymentStatus() == PaymentStatus.PAID) {
                throw new RuntimeException("Booking is already paid");
            }

            // Fetch the current Razorpay order details; orders can become invalid/expired (e.g. >24h old)
            JsonNode orderJson = fetchRazorpayOrder(payment.getRazorpayOrderId()).orElse(null);
            boolean needsNewOrder = true;
            long responseAmount = Math.round(payment.getFare() * 100);

            if (orderJson != null) {
                // Validate the Razorpay order before returning it to the frontend.
                if (isRazorpayOrderValid(orderJson, booking.getTotalFare())) {
                    needsNewOrder = false;
                    responseAmount = orderJson.get("amount").asLong();
                } else {
                    log.info("Existing Razorpay order {} is invalid/expired; creating a new order.", payment.getRazorpayOrderId());
                }
            } else {
                log.info("Unable to fetch Razorpay order {}. Creating a new order.", payment.getRazorpayOrderId());
            }

            if (needsNewOrder) {
                createRazorpayOrderAndSavePayment(payment, booking, request.getAmount());

                // Fetch the newly created order so we can return the exact amount/currency.
                JsonNode newOrderJson = fetchRazorpayOrder(payment.getRazorpayOrderId()).orElse(null);
                if (newOrderJson != null) {
                    responseAmount = newOrderJson.get("amount").asLong();
                }
            }

            PaymentOrderResponse responseDto = new PaymentOrderResponse();
            responseDto.setOrderId(payment.getRazorpayOrderId());
            responseDto.setAmount(responseAmount);
            responseDto.setCurrency(currency);
            responseDto.setKeyId(razorpayKeyId);
            return responseDto;
        }


        // Razorpay expects amount in paise
        long amountInPaise = Math.round(request.getAmount() * 100);

        // Build order payload
        JsonNode orderPayload = objectMapper.createObjectNode()
                .put("amount", amountInPaise)
                .put("currency", currency)
                .put("receipt", "booking_" + booking.getId())
                .put("payment_capture", 1);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest requestHttp = HttpRequest.newBuilder()
                .uri(URI.create(razorpayApiUrl + "/orders"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + basicAuth())
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(orderPayload)))
                .build();

        HttpResponse<String> response = client.send(requestHttp, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new RuntimeException("Failed to create Razorpay order: " + response.body());
        }

        JsonNode orderJson = objectMapper.readTree(response.body());
        String orderId = orderJson.get("id").asText();
        long returnedAmount = orderJson.get("amount").asLong();
        String returnedCurrency = orderJson.get("currency").asText();

        // Save a payment record with status PENDING
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setFare(request.getAmount());
        payment.setDistanceKm(booking.getRide().getDistanceKm());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setRazorpayOrderId(orderId);
        payment.setPaymentId(null);
        paymentRepository.save(payment);

        PaymentOrderResponse responseDto = new PaymentOrderResponse();
        responseDto.setOrderId(orderId);
        responseDto.setAmount(returnedAmount);
        responseDto.setCurrency(returnedCurrency);
        responseDto.setKeyId(razorpayKeyId);
        return responseDto;
    }

    private Optional<JsonNode> fetchRazorpayOrder(String orderId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest requestHttp = HttpRequest.newBuilder()
                    .uri(URI.create(razorpayApiUrl + "/orders/" + orderId))
                    .header("Authorization", "Basic " + basicAuth())
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(requestHttp, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Failed to fetch Razorpay order {}: status={} body={}", orderId, response.statusCode(), response.body());
                return Optional.empty();
            }

            return Optional.of(objectMapper.readTree(response.body()));
        } catch (Exception e) {
            log.warn("Failed to fetch Razorpay order {}: {}", orderId, e.getMessage());
            return Optional.empty();
        }
    }

    private boolean isRazorpayOrderValid(JsonNode orderJson, double expectedAmount) {
        if (orderJson == null || !orderJson.hasNonNull("id")) {
            return false;
        }

        String status = orderJson.path("status").asText();
        if (!"created".equalsIgnoreCase(status)) {
            log.warn("Razorpay order {} is not in 'created' status (status={})", orderJson.path("id").asText(), status);
            return false;
        }

        // Razorpay orders typically expire after ~24 hours. If the order is too old, create a new one.
        long createdAtSec = orderJson.path("created_at").asLong(0);
        if (createdAtSec > 0) {
            long ageSeconds = (System.currentTimeMillis() / 1000L) - createdAtSec;
            if (ageSeconds > 23 * 3600) {
                log.info("Razorpay order {} is older than 23 hours (ageSeconds={}), creating a fresh order.", orderJson.path("id").asText(), ageSeconds);
                return false;
            }
        }

        long expectedPaise = Math.round(expectedAmount * 100);
        long orderAmount = orderJson.path("amount").asLong(-1);
        if (orderAmount != expectedPaise) {
            log.warn("Razorpay order {} amount mismatch (order={} expected={})", orderJson.path("id").asText(), orderAmount, expectedPaise);
            return false;
        }

        return true;
    }

    private void createRazorpayOrderAndSavePayment(Payment payment, Booking booking, Double amount) throws Exception {
        long amountInPaise = Math.round(amount * 100);

        JsonNode orderPayload = objectMapper.createObjectNode()
                .put("amount", amountInPaise)
                .put("currency", currency)
                .put("receipt", "booking_" + booking.getId())
                .put("payment_capture", 1);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest requestHttp = HttpRequest.newBuilder()
                .uri(URI.create(razorpayApiUrl + "/orders"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + basicAuth())
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(orderPayload)))
                .build();

        HttpResponse<String> response = client.send(requestHttp, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new RuntimeException("Failed to create Razorpay order: " + response.body());
        }

        JsonNode orderJson = objectMapper.readTree(response.body());
        String orderId = orderJson.get("id").asText();

        payment.setRazorpayOrderId(orderId);
        paymentRepository.save(payment);
    }

    /**
     * Verify payment signature from Razorpay and update booking/payment status.
     */
    @Transactional
    public void verifyPayment(Long userId, VerifyPaymentRequest request) throws Exception {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getPassengerId().equals(userId)) {
            throw new RuntimeException("You can only verify payment for your own booking");
        }

        // Find payment record for this booking and order
        Payment payment = paymentRepository.findByPaymentId(request.getPaymentId())
                .orElseGet(() -> paymentRepository.findByRazorpayOrderId(request.getOrderId())
                        .orElseThrow(() -> new RuntimeException("Payment record not found")));

        // Verify signature
        if (!verifySignature(request.getOrderId(), request.getPaymentId(), request.getSignature())) {
            throw new RuntimeException("Invalid payment signature");
        }

        // Mark booking and payment as paid
        booking.setPaymentId(request.getPaymentId());
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        payment.setPaymentId(request.getPaymentId());
        payment.setPaymentStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);

        // Create transaction record for passenger (debit)
        createTransactionForPassenger(booking, payment);
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) throws Exception {
        String payload = orderId + "|" + paymentId;
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(razorpaySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] rawHmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        String expected = bytesToHex(rawHmac);
        boolean matches = expected.equalsIgnoreCase(signature);
        if (!matches) {
            log.warn("Razorpay signature mismatch (expected={} received={})", expected, signature);
        }
        return matches;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String h = Integer.toHexString(0xff & b);
            if (h.length() == 1) hex.append('0');
            hex.append(h);
        }
        return hex.toString();
    }

    private String basicAuth() {
        String creds = razorpayKeyId + ":" + razorpaySecret;
        return Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));
    }

    private void createTransactionForPassenger(Booking booking, Payment payment) {
        User passenger = userRepository.findById(booking.getPassengerId())
                .orElseThrow(() -> new RuntimeException("Passenger not found"));

        Transaction transaction = new Transaction();
        transaction.setUser(passenger);
        transaction.setPayment(payment);
        transaction.setAmount(-booking.getTotalFare());
        transaction.setBalanceAfter(null);
        transaction.setType(Transaction.TransactionType.DEBIT);
        transaction.setNote("Payment for booking " + booking.getId());

        transactionRepository.save(transaction);
    }

    /**
     * Called when a ride is completed to credit driver wallet.
     */
    @Transactional
    public void creditDriverOnRideCompletion(Booking booking) {
        User driver = userRepository.findById(booking.getRide().getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // Skip if booking wasn't paid
        if (booking.getPaymentStatus() != PaymentStatus.PAID) {
            return;
        }

        double driverAmount = booking.getTotalFare() * driverSharePercent;
        driver.setDriverWalletBalance(driver.getDriverWalletBalance() + driverAmount);
        userRepository.save(driver);

        Transaction tx = new Transaction();
        tx.setUser(driver);
        tx.setPayment(paymentRepository.findByBookingId(booking.getId()).orElse(null));
        tx.setAmount(driverAmount);
        tx.setBalanceAfter(driver.getDriverWalletBalance());
        tx.setType(Transaction.TransactionType.CREDIT);
        tx.setNote("Driver payout for booking " + booking.getId());
        transactionRepository.save(tx);
    }
}
