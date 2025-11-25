package com.mutsa.springboot_auction.domain.notification.controller;

import com.mutsa.springboot_auction.domain.notification.dto.NotificationResponse;
import com.mutsa.springboot_auction.domain.notification.service.NotificationService;
import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
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
    public SseEmitter subscribe(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        Long userId = customOAuth2User.getUser().getId();
        return notificationService.subscribe(userId);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        Long userId = customOAuth2User.getUser().getId();
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @GetMapping("/list")
    public ResponseEntity<List<NotificationResponse>> getNotificationList(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User
    ) {
        return ResponseEntity.ok(notificationService.getNotificationList(customOAuth2User.getUser().getId()));
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
