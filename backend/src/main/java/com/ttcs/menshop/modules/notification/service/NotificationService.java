package com.ttcs.menshop.modules.notification.service;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.modules.notification.converter.NotificationConverter;
import com.ttcs.menshop.modules.notification.dto.NotificationResponse;
import com.ttcs.menshop.modules.notification.entity.NotificationEntity;
import com.ttcs.menshop.modules.notification.repository.NotificationRepository;
import com.ttcs.menshop.modules.order.entity.OrderEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationConverter notificationConverter;
    private final SimpMessagingTemplate messagingTemplate;

    public void createAndSend(
            UserEntity user,
            OrderEntity order,
            String type,
            String title,
            String message
    ) {
        NotificationEntity n = new NotificationEntity();
        n.setUser(user);
        n.setOrder(order);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(n);

        NotificationResponse res = notificationConverter.toResponse(n);

        messagingTemplate.convertAndSend(
                "/topic/user/" + user.getId() + "/notifications",
                res
        );
    }

    public List<NotificationResponse> getMyNotifications(Integer userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notificationConverter::toResponse)
                .toList();
    }

    public void markAsRead(Integer id, Integer userId) {
        NotificationEntity n = notificationRepository.findById(id)
                .orElseThrow();

        if (!n.getUser().getId().equals(userId)) {
            throw new RuntimeException("Forbidden");
        }

        n.setIsRead(true);
        notificationRepository.save(n);
    }
}
