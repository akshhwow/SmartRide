package com.smartride;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ============================================================
 * MAIN APPLICATION CLASS - This is where your app starts!
 * ============================================================
 *
 * 📌 Think of this as the "main door" of your application.
 *    When you run this class, your Spring Boot server starts up.
 *
 * @SpringBootApplication = Tells Spring Boot this is the main class
 * @EnableScheduling = Allows scheduled tasks (like OTP cleanup)
 * ============================================================
 */
@SpringBootApplication
@EnableScheduling
public class SmartRideApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartRideApplication.class, args);
		System.out.println("====================================================");
		System.out.println("  🚗 Smart Ride Sharing System - Backend Started!   ");
		System.out.println("  📡 Server running on: http://localhost:8080       ");
		System.out.println("====================================================");
	}
}
