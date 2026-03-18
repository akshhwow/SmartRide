package com.smartride.service;

import com.smartride.entity.Notification;
import com.smartride.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate; // Handles WebSocket pushes

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
