package com.smartride.exception;

import com.smartride.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * GlobalExceptionHandler - Catches ALL Errors in One Place
 * ============================================================
 *
 * 📌 What is this?
 *    When your code throws an exception (error), Spring normally
 *    returns an ugly technical error response.
 *    This class intercepts ALL exceptions and returns clean,
 *    user-friendly JSON responses instead.
 *
 * 📌 @RestControllerAdvice = Applies to ALL controllers globally
 * 📌 @ExceptionHandler = "When THIS exception occurs, run this method"
 *
 * 📌 IMPORTANT: Handler ORDER matters!
 *    Spring checks handlers from most specific → least specific.
 *    RuntimeException MUST come BEFORE Exception in the class,
 *    otherwise Exception catches everything first (too broad).
 *
 * 📌 FIX APPLIED:
 *    1. Added NoResourceFoundException handler → gives a clear 404
 *       message instead of falling through to the generic 500 handler.
 *       This would have caught your "No static resource api/bookings" error
 *       with a proper 404 instead of a confusing 500.
 *    2. Moved RuntimeException handler BEFORE the generic Exception handler
 *       so our custom runtime errors (from services) return 400 Bad Request,
 *       not 500 Internal Server Error.
 * ============================================================
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ============================================================
     * Handle @Valid validation failures
     * ============================================================
     * Triggered when: Request body fails @Valid annotation checks
     * Example: Required field is missing, email format is wrong
     *
     * Returns 400 Bad Request with a map of field → error message
     * Example response:
     * {
     *   "success": false,
     *   "message": "Please fix the following errors:",
     *   "data": { "email": "Email is required", "password": "Too short" }
     * }
     * ============================================================
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed: {}", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Please fix the following errors:")
                        .data(errors)
                        .build());
    }

    /**
     * ============================================================
     * Handle wrong password/email during login
     * ============================================================
     * Triggered when: authenticationManager.authenticate() fails
     * Returns 401 Unauthorized
     * ============================================================
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials attempt: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid email or password."));
    }

    /**
     * ============================================================
     * Handle access denied errors
     * ============================================================
     * Triggered when: User tries to access something they're not
     *                 allowed to (e.g. passenger trying to post a ride)
     * Returns 403 Forbidden
     * ============================================================
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You don't have permission to access this resource."));
    }

    /**
     * ============================================================
     * ✅ FIX: Handle "No static resource" / endpoint not found
     * ============================================================
     * Triggered when: A URL doesn't match any controller endpoint
     * Example: POST /api/bookings when BookingController didn't exist
     *
     * BEFORE THIS FIX: It fell through to the generic Exception handler
     * and returned 500 "Something went wrong" — very confusing!
     *
     * NOW: Returns a clear 404 "Endpoint not found" message
     * Returns 404 Not Found
     * ============================================================
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("Endpoint not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(
                        "Endpoint not found: " + ex.getMessage() +
                                ". Please check the URL and HTTP method."));
    }

    /**
     * ============================================================
     * ✅ FIX: Handle custom RuntimeExceptions from services
     * ============================================================
     * Triggered when: Your service code throws new RuntimeException("...")
     * Examples:
     *   - "Ride not found with ID: 5"
     *   - "You have already booked this ride"
     *   - "Not enough seats available"
     *   - "Only drivers can post rides"
     *
     * IMPORTANT: This MUST be listed BEFORE the generic Exception handler!
     * If Exception comes first, it catches RuntimeException too (since
     * RuntimeException extends Exception), and you'd always get 500 errors
     * instead of the proper 400 Bad Request.
     *
     * Returns 400 Bad Request with the exact message from the service.
     * ============================================================
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.warn("Business logic error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * ============================================================
     * Handle all other unexpected errors (safety net)
     * ============================================================
     * Triggered when: An error that isn't caught by the above handlers
     * Example: NullPointerException, database connection issues
     *
     * Returns 500 Internal Server Error
     * Note: The full stack trace is logged for debugging,
     *       but we only return a generic message to the user
     *       (never expose technical details to the client).
     * ============================================================
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Something went wrong. Please try again later."));
    }
}