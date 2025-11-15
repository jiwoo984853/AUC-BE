package com.mutsa.springboot_auction.domain.notification.controller;

import com.mutsa.springboot_auction.domain.notification.dto.NotificationResponse;
import com.mutsa.springboot_auction.domain.notification.service.NotificationService;
import com.mutsa.springboot_auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal(expression = "user") User user) {
        Long userId = user.getId();
        return notificationService.subscribe(userId);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(
            @AuthenticationPrincipal(expression = "user") User user) {
        Long userId = user.getId();
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @GetMapping("/list")
    public ResponseEntity<List<NotificationResponse>> getNotificationList(
            @AuthenticationPrincipal(expression = "user") User user
    ) {
        return ResponseEntity.ok(notificationService.getNotificationList(user.getId()));
    }

    @GetMapping("/unread-count/test")
    public ResponseEntity<Integer> getUnreadCountTest(
            @RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @GetMapping("/list/test")
    public ResponseEntity<List<NotificationResponse>> getNotificationList(
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(notificationService.getNotificationList(userId));
    }


}
