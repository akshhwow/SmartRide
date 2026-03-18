package com.smartride.service;

import com.smartride.dto.request.LoginRequest;
import com.smartride.dto.request.RegisterRequest;
import com.smartride.dto.request.VerifyOtpRequest;
import com.smartride.dto.response.ApiResponse;
import com.smartride.dto.response.AuthResponse;
import com.smartride.entity.EmailOtp;
import com.smartride.entity.User;
import com.smartride.repository.EmailOtpRepository;
import com.smartride.repository.UserRepository;
import com.smartride.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * ============================================================
 * AuthService - Handles all authentication operations
 * ============================================================
 *
 * This service manages:
 * - User registration (Driver & Passenger)
 * - Email OTP verification
 * - User login with JWT token generation
 * - OTP resending
 *
 * 📌 FIXES APPLIED:
 *    1. verifyOtp now takes VerifyOtpRequest (to match AuthController call)
 *    2. ApiResponse import fixed to com.smartride.dto.response.ApiResponse
 *    3. jwtService.generateToken(user) now works because User implements UserDetails
 * ============================================================
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${jwt.otp.expiry:10}")
    private int otpExpiryMinutes;

    /**
     * ============================================================
     * REGISTER a new user (Driver or Passenger)
     * ============================================================
     * Steps:
     *  1. Check email not already registered
     *  2. Check phone not already registered
     *  3. Create user with encrypted password
     *  4. If DRIVER, save vehicle details
     *  5. Generate OTP and send verification email
     * ============================================================
     */
    @Transactional
    public ApiResponse<String> register(RegisterRequest request) {

        // Step 1: Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ApiResponse.error(
                    "Email already registered. Please login or use a different email.");
        }

        // Step 2: Check if phone already exists
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            return ApiResponse.error(
                    "Phone number already registered. Please use a different number.");
        }

        // Step 3: Create new user object
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Encrypt password!
        user.setRole(request.getRole());
        user.setEmailVerified(false); // Not verified yet

        // Step 4: Set driver-specific fields if role is DRIVER
        if (request.getRole() == User.Role.DRIVER) {
            if (request.getCarModel() == null || request.getLicensePlate() == null) {
                return ApiResponse.error(
                        "Driver must provide vehicle details (car model and license plate)");
            }
            user.setCarModel(request.getCarModel());
            user.setLicensePlate(request.getLicensePlate());
            user.setVehicleCapacity(request.getVehicleCapacity());
            user.setVehicleType(request.getVehicleType());
        }

        // Step 5: Save user to database
        User savedUser = userRepository.save(user);

        // Step 6: Generate and send OTP
        String otpCode = generateOTP();

        try {
            // Save OTP to database
            EmailOtp emailOtp = EmailOtp.builder()
                    .email(savedUser.getEmail())
                    .otpCode(otpCode)
                    .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                    .createdAt(LocalDateTime.now())
                    .verified(false)
                    .isUsed(false)
                    .build();

            emailOtpRepository.save(emailOtp);

            // Send OTP verification email
            emailService.sendOtpEmail(savedUser.getEmail(), savedUser.getFullName(), otpCode);

            return ApiResponse.success(
                    "Registration successful! Please check your email for the 6-digit verification code.",
                    "VERIFICATION_EMAIL_SENT"
            );

        } catch (Exception e) {
            // If email sending fails, still allow registration but inform user
            return ApiResponse.success(
                    "Account created! However, we couldn't send the verification email. " +
                            "Please use 'Resend OTP' option.",
                    "EMAIL_SEND_FAILED"
            );
        }
    }

    /**
     * ============================================================
     * VERIFY OTP - Confirm the user's email address
     * ============================================================
     *
     * 📌 FIX: Method now accepts VerifyOtpRequest object
     *    (was: String email, String otpCode — mismatched with AuthController)
     *    AuthController calls: authService.verifyOtp(request)
     *    So this method must accept the VerifyOtpRequest DTO directly.
     *
     * Steps:
     *  1. Find user by email
     *  2. Check if already verified
     *  3. Find and validate the OTP
     *  4. Mark OTP as used
     *  5. Mark user as verified
     *  6. Generate JWT token and auto-login
     * ============================================================
     */
    @Transactional
    public ApiResponse<AuthResponse> verifyOtp(VerifyOtpRequest request) {

        String email = request.getEmail();
        String otpCode = request.getOtpCode();

        // Step 1: Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Step 2: Check if already verified
        if (user.getEmailVerified()) {
            return ApiResponse.error("Email already verified. Please login.");
        }

        // Step 3: Find valid (unused) OTP
        EmailOtp emailOtp = emailOtpRepository
                .findByEmailAndOtpCodeAndIsUsedFalse(email, otpCode)
                .orElseThrow(() -> new RuntimeException(
                        "Invalid OTP code. Please check your email or request a new OTP."));

        // Step 4: Check if OTP is expired
        if (emailOtp.isExpired()) {
            return ApiResponse.error("OTP has expired. Please request a new one.");
        }

        // Step 5: Mark OTP as used
        emailOtp.setIsUsed(true);
        emailOtp.setVerified(true);
        emailOtpRepository.save(emailOtp);

        // Step 6: Mark user email as verified
        user.setEmailVerified(true);
        userRepository.save(user);

        // Step 7: Generate JWT token — auto-login after verification
        // ✅ This works now because User implements UserDetails
        String token = jwtService.generateToken(user);

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(convertToUserResponse(user))
                .build();

        return ApiResponse.success(
                "Email verified successfully! Welcome to SmartRide 🚗",
                authResponse
        );
    }

    /**
     * ============================================================
     * LOGIN - Authenticate user and return JWT token
     * ============================================================
     * Steps:
     *  1. Find user by email
     *  2. Check if email is verified
     *  3. Authenticate with Spring Security (checks password)
     *  4. Generate JWT token
     *  5. Return token + user info
     * ============================================================
     */
    @Transactional
    public ApiResponse<AuthResponse> login(LoginRequest request) {

        // Step 1: Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Step 2: Check if email is verified
        if (!user.getEmailVerified()) {
            return ApiResponse.error(
                    "Please verify your email before logging in. " +
                            "Check your inbox for the verification code.");
        }

        try {
            // Step 3: Authenticate (Spring checks password using BCrypt)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Step 4: Generate JWT token
            // ✅ This works now because User implements UserDetails
            String token = jwtService.generateToken(user);

            AuthResponse authResponse = AuthResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .user(convertToUserResponse(user))
                    .build();

            return ApiResponse.success(
                    "Login successful! Welcome back, " + user.getFullName() + "!",
                    authResponse
            );

        } catch (Exception e) {
            return ApiResponse.error("Invalid email or password");
        }
    }

    /**
     * ============================================================
     * RESEND OTP - Send a fresh OTP to the user's email
     * ============================================================
     */
    @Transactional
    public ApiResponse<String> resendOtp(String email) {

        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with email: " + email));

        // Check if already verified
        if (user.getEmailVerified()) {
            return ApiResponse.error("Email already verified. Please login.");
        }

        // Mark all previous OTPs for this email as used (invalidate old OTPs)
        emailOtpRepository.findByEmail(email).forEach(otp -> {
            otp.setIsUsed(true);
            emailOtpRepository.save(otp);
        });

        // Generate a fresh OTP
        String otpCode = generateOTP();

        try {
            // Save new OTP to database
            EmailOtp emailOtp = EmailOtp.builder()
                    .email(user.getEmail())
                    .otpCode(otpCode)
                    .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                    .createdAt(LocalDateTime.now())
                    .verified(false)
                    .isUsed(false)
                    .build();

            emailOtpRepository.save(emailOtp);

            // Send new OTP email
            emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otpCode);

            return ApiResponse.success(
                    "New OTP has been sent to your email!",
                    "OTP_SENT"
            );

        } catch (Exception e) {
            return ApiResponse.error("Failed to send OTP. Please try again later.");
        }
    }

    /**
     * Generate a random 6-digit OTP
     * Example output: "482931"
     */
    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Convert User entity → UserResponse DTO (for sending to frontend)
     * We never send the full User entity to the frontend (security risk!)
     */
    private AuthResponse.UserResponse convertToUserResponse(User user) {
        AuthResponse.UserResponse userResponse = new AuthResponse.UserResponse();
        userResponse.setId(user.getId());
        userResponse.setFullName(user.getFullName());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhone(user.getPhone());
        userResponse.setRole(user.getRole());
        userResponse.setEmailVerified(user.getEmailVerified());

        // Add driver-specific fields if user is a driver
        if (user.getRole() == User.Role.DRIVER) {
            userResponse.setCarModel(user.getCarModel());
            userResponse.setLicensePlate(user.getLicensePlate());
            userResponse.setVehicleCapacity(user.getVehicleCapacity());
            userResponse.setVehicleType(user.getVehicleType());
        }

        return userResponse;
    }
}