package com.smartride.service;

import com.smartride.entity.Notification;
import com.smartride.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import com.smartride.entity.User;
import com.smartride.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate; // Handles WebSocket pushes
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    /**
     * Save to DB then push live to the specific user via WebSockets.
     */
    public void saveAndEmit(Long recipientId, Long senderId, String type, Long rideId, String message) {
        // 1. Save to database
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setSenderId(senderId);
        notification.setType(type);
        notification.setRideId(rideId);
        notification.setMessage(message);
        notification.setIsRead(false);

        Notification saved = notificationRepository.save(notification);

        // 2. Emit to WebSocket topic specifically for this recipient
        // The frontend will subscribe to: /topic/user/{userId}
        String destination = "/topic/user/" + recipientId;
        log.info("Emitting to destination: {} | message: {}", destination, message);
        messagingTemplate.convertAndSend(destination, saved);
        
        // 3. Send Email for important events
        if ("BOOKING_CONFIRMED".equals(type) || "CANCELLED".equals(type) || "RESCHEDULED".equals(type) || "REMINDER".equals(type)) {
            sendEmailNotification(recipientId, type, message);
        }
    }
    
    private void sendEmailNotification(Long recipientId, String type, String text) {
        try {
            User user = userRepository.findById(recipientId).orElse(null);
            if (user != null && user.getEmail() != null) {
                SimpleMailMessage email = new SimpleMailMessage();
                email.setTo(user.getEmail());
                email.setSubject("SmartRide Update: " + type.replace('_', ' '));
                email.setText(text);
                mailSender.send(email);
                log.info("Email sent to user: {}", user.getEmail());
            }
        } catch (Exception e) {
            log.error("Failed to send email notification to userId {}: {}", recipientId, e.getMessage());
        }
    }

    public List<Notification> getRecentNotifications(Long userId) {
        return notificationRepository.findTop20ByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }

    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findTop20ByRecipientIdOrderByCreatedAtDesc(userId)
            .stream().filter(n -> !n.getIsRead()).toList();
        
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }
}
