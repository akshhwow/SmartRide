package com.smartride.service;

import com.smartride.entity.*;
import com.smartride.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RideRepository rideRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User toggleUserStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(isActive);
        return userRepository.save(user);
    }

    public User verifyDriver(Long driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        if (driver.getRole() != User.Role.DRIVER) {
            throw new RuntimeException("User is not a driver");
        }
        driver.setVerified(true);
        return userRepository.save(driver);
    }

    public Page<Ride> getAllRides(Pageable pageable) {
        return rideRepository.findAll(pageable);
    }

    public Page<Booking> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Map<String, Object> getReports() {
        Map<String, Object> reports = new HashMap<>();

        long totalRides = rideRepository.count();
        long activeUsers = userRepository.findAll().stream().filter(User::getIsActive).count();
        
        List<Booking> allBookings = bookingRepository.findAll();
        long totalBookings = allBookings.size();
        long cancelledBookings = allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CANCELLED).count();
        
        double cancellationRate = totalBookings > 0 ? ((double) cancelledBookings / totalBookings) * 100 : 0.0;

        double totalEarnings = paymentRepository.findAll().stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID)
                .mapToDouble(Payment::getFare)
                .sum();

        // Existing stats
        reports.put("totalRides", totalRides);
        reports.put("activeUsers", activeUsers);
        reports.put("totalBookings", totalBookings);
        reports.put("cancellationRate", cancellationRate);
        reports.put("totalPlatformEarnings", totalEarnings);

        // Advanced reports: Top drivers by rating
        List<User> topDrivers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.DRIVER && u.getRating() != null && u.getTotalRatings() != null && u.getTotalRatings() > 0)
                .sorted(Comparator.comparing(User::getRating).reversed())
                .limit(5)
                .collect(Collectors.toList());
        reports.put("topDrivers", topDrivers);

        // Most active users (by bookings made OR rides posted)
        // For simplicity, top passengers by number of bookings
        Map<Long, Long> userBookingCounts = allBookings.stream()
                .collect(Collectors.groupingBy(Booking::getPassengerId, Collectors.counting()));
        
        List<Map<String, Object>> mostActiveUsers = userBookingCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", e.getKey());
                    map.put("bookingsCount", e.getValue());
                    return map;
                })
                .collect(Collectors.toList());
        reports.put("mostActiveUsers", mostActiveUsers);

        // Revenue per day (last 7 days could be better, but simple version)
        double revenueToday = paymentRepository.findAll().stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID && p.getCreatedAt() != null && p.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                .mapToDouble(Payment::getFare)
                .sum();
        reports.put("revenueToday", revenueToday);

        return reports;
    }
}
