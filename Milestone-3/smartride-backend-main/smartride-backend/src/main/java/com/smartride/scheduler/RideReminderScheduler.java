package com.smartride.scheduler;

import com.smartride.entity.Booking;
import com.smartride.entity.Ride;
import com.smartride.repository.BookingRepository;
import com.smartride.repository.RideRepository;
import com.smartride.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RideReminderScheduler {

    private final RideRepository rideRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    // Run every 30 minutes to check for upcoming rides
    @Scheduled(cron = "0 0/30 * * * ?")
    public void sendRideReminders() {
        log.info("Running scheduled task: Checking for upcoming rides in the next 2 hours...");
        
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalTime twoHoursLater = now.plusHours(2).plusMinutes(1);

        // Fetch all active rides starting today
        List<Ride> upcomingRides = rideRepository.findByRideDateAndStatus(today, Ride.RideStatus.ACTIVE);

        List<Ride> ridesToNotify = upcomingRides.stream()
                .filter(r -> r.getDepartureTime().isAfter(now) && r.getDepartureTime().isBefore(twoHoursLater))
                .collect(Collectors.toList());

        for (Ride ride : ridesToNotify) {
            String timeStr = ride.getDepartureTime().toString();
            
            // Notify Driver
            notificationService.saveAndEmit(
                    ride.getDriverId(),
                    ride.getDriverId(),
                    "REMINDER",
                    ride.getId(),
                    "Reminder: Your scheduled ride from " + ride.getSource() + " departs at " + timeStr
            );

            // Notify all confirmed passengers
            List<Booking> confirmedBookings = bookingRepository.findByRideIdAndStatus(ride.getId(), Booking.BookingStatus.CONFIRMED);
            
            for (Booking booking : confirmedBookings) {
                notificationService.saveAndEmit(
                        booking.getPassengerId(),
                        ride.getDriverId(),
                        "REMINDER",
                        ride.getId(),
                        "Reminder: Your booked ride from " + ride.getSource() + " departs at " + timeStr
                );
            }
        }
        
        log.info("Sent reminders for {} upcoming rides.", ridesToNotify.size());
    }
}
