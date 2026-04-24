package com.ttcs.menshop.modules.notification.controller;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.modules.notification.converter.NotificationConverter;
import com.ttcs.menshop.modules.notification.dto.NotificationResponse;
import com.ttcs.menshop.modules.notification.entity.NotificationEntity;
import com.ttcs.menshop.modules.notification.repository.NotificationRepository;
import com.ttcs.menshop.modules.notification.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NotificationController {

    private final AuthService authService;
    private final NotificationRepository notificationRepository;
    private final NotificationConverter notificationConverter;
    private final NotificationService notificationService;

    public NotificationController(AuthService authService, NotificationRepository notificationRepository, NotificationConverter notificationConverter, NotificationService notificationService) {
        this.authService = authService;
        this.notificationRepository = notificationRepository;
        this.notificationConverter = notificationConverter;
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications/my")
    public ResponseEntity<?> getMyNotifications() {
        UserEntity user = authService.getCurrentUser();
        return ResponseEntity.ok(
                notificationService.getMyNotifications(user.getId())
        );
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Integer id
    ) {
        UserEntity user = authService.getCurrentUser();
        notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok("OK");
    }
}
