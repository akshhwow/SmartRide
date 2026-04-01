package com.smartride.controller;

import com.smartride.dto.response.ApiResponse;
import com.smartride.entity.*;
import com.smartride.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Fetched all users", adminService.getAllUsers(pageable)));
    }

    @PatchMapping("/users/{userId}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> toggleUserStatus(@PathVariable Long userId, @RequestParam boolean active) {
        return ResponseEntity.ok(ApiResponse.success("User status toggled", adminService.toggleUserStatus(userId, active)));
    }

    @PatchMapping("/drivers/{driverId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> verifyDriver(@PathVariable Long driverId) {
        return ResponseEntity.ok(ApiResponse.success("Driver verified successfully", adminService.verifyDriver(driverId)));
    }

    @GetMapping("/rides")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<Ride>>> getAllRides(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Fetched all rides", adminService.getAllRides(pageable)));
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<Booking>>> getAllBookings(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Fetched all bookings", adminService.getAllBookings(pageable)));
    }

    @GetMapping("/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Payment>>> getAllPayments() {
        return ResponseEntity.ok(ApiResponse.success("Fetched all payments", adminService.getAllPayments()));
    }

    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReports() {
        return ResponseEntity.ok(ApiResponse.success("Fetched admin reports", adminService.getReports()));
    }
}
