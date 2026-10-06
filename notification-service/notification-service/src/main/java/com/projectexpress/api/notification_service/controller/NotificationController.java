package com.projectexpress.api.notification_service.controller;

import com.projectexpress.api.notification_service.entity.Notification;
import com.projectexpress.api.notification_service.security.AuthenticatedUser;
import com.projectexpress.api.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        Long candidateId = user.getUserId();

        return ResponseEntity.ok(notificationService.getNotifications(candidateId));
    }


    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        Long candidateId = user.getUserId();
        return ResponseEntity.ok(notificationService.getUnreadNotifications(candidateId));
    }


    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long notificationId, Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        Long candidateId = user.getUserId();
        return ResponseEntity.ok(notificationService.markAsRead(notificationId, candidateId));
    }


    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        Long candidateId = user.getUserId();
        notificationService.markAllAsRead(candidateId);
        return ResponseEntity.noContent().build();
    }
}