// ============================================================
// DTOs (Data Transfer Objects) - What data goes IN and OUT of APIs
// ============================================================
// 📌 What is a DTO?
//    DTO = Data Transfer Object
//    These are simple classes that define WHAT DATA you send
//    to the API (request) and what the API sends back (response).
//
//    WHY not use Entity directly?
//    - Entities have sensitive fields (password, etc.)
//    - DTOs let you control exactly what data is exposed
//    - Different operations need different subsets of data
// ============================================================

package com.smartride.dto;

import com.smartride.entity.User;
import jakarta.validation.constraints.*;

// ============================================================
// 1. REGISTRATION REQUEST - Data sent when user signs up
// ============================================================
// This is in a separate file in real projects, but combined here for clarity
// Create this as: dto/request/RegisterRequest.java
// ============================================================

// Package for all DTOs: com.smartride.dto.request / com.smartride.dto.response

class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotNull(message = "Role is required")
    private User.Role role; // DRIVER or PASSENGER

    // DRIVER-ONLY FIELDS (required only if role = DRIVER)
    private String carModel;
    private String licensePlate;
    private Integer vehicleCapacity;
    private String vehicleType;
}
