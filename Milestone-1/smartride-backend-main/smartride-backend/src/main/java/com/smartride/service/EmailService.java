package com.smartride.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * ============================================================
 * EmailService - Handles email sending operations
 * ============================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send OTP verification email
     */
    public void sendOtpEmail(String toEmail, String userName, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("SmartRide - Email Verification Code");
            message.setText(buildOtpEmailBody(userName, otpCode));

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Build OTP email body
     */
    private String buildOtpEmailBody(String userName, String otpCode) {
        return String.format("""
            Hello %s,
            
            Welcome to SmartRide! 🚗
            
            Your email verification code is: %s
            
            This code will expire in 10 minutes.
            
            If you didn't create an account with SmartRide, please ignore this email.
            
            Best regards,
            The SmartRide Team
            """, userName, otpCode);
    }

    /**
     * Send booking confirmation email
     */
    public void sendBookingConfirmationEmail(String toEmail, String userName, String rideDetails) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("SmartRide - Booking Confirmation");
            message.setText(String.format("""
                Hello %s,
                
                Your ride has been confirmed! 🎉
                
                %s
                
                Safe travels!
                
                Best regards,
                The SmartRide Team
                """, userName, rideDetails));

            mailSender.send(message);
            log.info("Booking confirmation email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email to: {}", toEmail, e);
        }
    }
}