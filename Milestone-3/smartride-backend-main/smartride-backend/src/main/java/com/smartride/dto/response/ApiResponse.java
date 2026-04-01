package com.smartride.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================
 * ApiResponse - Standard response wrapper for ALL API responses
 * ============================================================
 * 📌 Every API response is wrapped in this class.
 *    This ensures consistent response format across the entire app.
 *
 *    Success example:
 *    {
 *      "success": true,
 *      "message": "Registration successful",
 *      "data": { ...user info... }
 *    }
 *
 *    Error example:
 *    {
 *      "success": false,
 *      "message": "Email already exists",
 *      "data": null
 *    }
 *
 * @param <T> = The type of data in the response (can be anything)
 * ============================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // Quick helper to create success response
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    // Quick helper to create error response
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
}
