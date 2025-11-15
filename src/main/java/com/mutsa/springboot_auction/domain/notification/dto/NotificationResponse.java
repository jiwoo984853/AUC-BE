package com.mutsa.springboot_auction.domain.notification.dto;


import com.mutsa.springboot_auction.domain.notification.entity.Notification;
import com.mutsa.springboot_auction.domain.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String message,
        Long auctionId,
        NotificationType type,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getMessage(),
                notification.getAuctionId(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
