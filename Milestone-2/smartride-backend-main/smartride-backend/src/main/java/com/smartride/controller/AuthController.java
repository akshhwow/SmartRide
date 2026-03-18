package com.smartride.controller;

import com.smartride.dto.request.LoginRequest;
import com.smartride.dto.request.RegisterRequest;
import com.smartride.dto.request.VerifyOtpRequest;
import com.smartride.dto.response.ApiResponse;
import com.smartride.dto.response.AuthResponse;
import com.smartride.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================
 * AuthController - HTTP Endpoints for Authentication
 * ============================================================
 *
 * 📌 What is a Controller?
 *    A Controller handles incoming HTTP requests from:
 *    - Your React frontend
 *    - Postman (for testing)
 *
 *    It receives the request, calls the appropriate Service method,
 *    and sends back the response.
 *
 * 📌 @RestController = Controller that returns JSON (not HTML pages)
 * 📌 @RequestMapping("/api/auth") = All URLs in this class start with /api/auth
 * 📌 @RequiredArgsConstructor = Lombok auto-creates constructor (for injection)
 * 📌 @CrossOrigin = Allows requests from React frontend
 *
 * 📌 Available Endpoints:
 *    POST /api/auth/register     → Register new user
 *    POST /api/auth/verify-otp   → Verify email with OTP
 *    POST /api/auth/login        → Login and get JWT token
 *    POST /api/auth/resend-otp   → Request a new OTP
 *
 * 📌 All these endpoints are PUBLIC (no JWT needed)
 *    because they're whitelisted in SecurityConfig.
 * ============================================================
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * ============================================================
     * POST /api/auth/register
     * ============================================================
     *
     * Register a new Driver or Passenger account.
     *
     * 📌 Request Body (JSON):
     * {
     *   "fullName": "John Doe",
     *   "email": "john@example.com",
     *   "phone": "9876543210",
     *   "password": "mypassword123",
     *   "role": "PASSENGER"
     * }
     *
     * For DRIVER, also include:
     * {
     *   ...above fields,
     *   "role": "DRIVER",
     *   "carModel": "Honda Civic",
     *   "licensePlate": "MH-12-AB-1234",
     *   "vehicleCapacity": 4,
     *   "vehicleType": "Sedan"
     * }
     *
     * 📌 Response: Success message + tells user to check email for OTP
     * ============================================================
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * ============================================================
     * POST /api/auth/verify-otp
     * ============================================================
     *
     * Verify the user's email using the 6-digit OTP sent to their inbox.
     * After successful verification, user is automatically logged in
     * and receives a JWT token.
     *
     * 📌 Request Body (JSON):
     * {
     *   "email": "john@example.com",
     *   "otpCode": "482931"
     * }
     *
     * 📌 Response: JWT token + user details (same as login response)
     * ============================================================
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(
            @RequestBody VerifyOtpRequest request) {

        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    /**
     * ============================================================
     * POST /api/auth/login
     * ============================================================
     *
     * Login with email and password.
     * Returns a JWT token that the frontend stores and sends with
     * every future request.
     *
     * 📌 Request Body (JSON):
     * {
     *   "email": "john@example.com",
     *   "password": "mypassword123"
     * }
     *
     * 📌 Response:
     * {
     *   "success": true,
     *   "message": "Login successful! Welcome back, John Doe!",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiJ9...",
     *     "tokenType": "Bearer",
     *     "user": { "id": 1, "fullName": "John Doe", "role": "PASSENGER", ... }
     *   }
     * }
     * ============================================================
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * ============================================================
     * POST /api/auth/resend-otp?email=john@example.com
     * ============================================================
     *
     * Resend a new OTP to the user's email.
     * Use this if the previous OTP expired or wasn't received.
     *
     * 📌 Request: Query parameter (not body)
     *    URL: POST /api/auth/resend-otp?email=john@example.com
     *
     * 📌 Response: Success message confirming OTP was sent
     * ============================================================
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendOtp(
            @RequestParam String email) {

        return ResponseEntity.ok(authService.resendOtp(email));
    }

    /**
     * ============================================================
     * GET /api/auth/dev-otp?email=<email>
     *
     * Development helper: return the most recent OTP for the given email.
     * This is disabled by default; enable it via app.dev.allow-otp-retrieval=true.
     * ============================================================
     */
    @GetMapping("/dev-otp")
    public ResponseEntity<ApiResponse<String>> getLatestOtp(@RequestParam String email) {
        return ResponseEntity.ok(authService.getLatestOtpForEmail(email));
    }
}
