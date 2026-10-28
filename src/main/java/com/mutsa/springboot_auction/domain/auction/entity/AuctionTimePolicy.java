package com.mutsa.springboot_auction.domain.auction.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class AuctionTimePolicy {

    private static final ZoneId AUCTION_ZONE = ZoneId.of("Asia/Seoul");

    private AuctionTimePolicy() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(AUCTION_ZONE);
    }

    public static boolean hasNotStarted(Auction auction, LocalDateTime now) {
        return auction.getStartAt() != null && now.isBefore(auction.getStartAt());
    }

    public static boolean hasEnded(Auction auction, LocalDateTime now) {
        return auction.getEndAt() != null && !now.isBefore(auction.getEndAt());
    }

    public static AuctionStatus effectiveStatus(Auction auction) {
        return effectiveStatus(auction, now());
    }

    public static AuctionStatus effectiveStatus(Auction auction, LocalDateTime now) {
        if (auction.getStatus() == AuctionStatus.IN_PROGRESS && hasEnded(auction, now)) {
            return AuctionStatus.CLOSED;
        }
        return auction.getStatus();
    }
}
