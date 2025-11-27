package com.mutsa.springboot_auction.domain.notification.entity;

public enum NotificationType {
    OUTBID, // 상위입찰
    WIN, // 낙찰
    FAIL, //입찰자 없이 종료

    NEXT_WINNER_OFFER//다음 입찰자에게 알림
}
