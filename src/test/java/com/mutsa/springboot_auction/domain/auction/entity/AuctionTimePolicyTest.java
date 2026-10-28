package com.mutsa.springboot_auction.domain.auction.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class AuctionTimePolicyTest {

    private static final LocalDateTime END_AT = LocalDateTime.of(2026, 10, 28, 15, 0);

    @Test
    void auctionIsOpenImmediatelyBeforeEndTime() {
        Auction auction = auctionEndingAt(END_AT);

        assertFalse(AuctionTimePolicy.hasEnded(auction, END_AT.minusNanos(1)));
        assertEquals(
                AuctionStatus.IN_PROGRESS,
                AuctionTimePolicy.effectiveStatus(auction, END_AT.minusNanos(1))
        );
    }

    @Test
    void auctionIsClosedAtExactEndTime() {
        Auction auction = auctionEndingAt(END_AT);

        assertTrue(AuctionTimePolicy.hasEnded(auction, END_AT));
        assertEquals(AuctionStatus.CLOSED, AuctionTimePolicy.effectiveStatus(auction, END_AT));
    }

    @Test
    void auctionIsClosedAfterEndTime() {
        Auction auction = auctionEndingAt(END_AT);

        assertTrue(AuctionTimePolicy.hasEnded(auction, END_AT.plusNanos(1)));
        assertEquals(
                AuctionStatus.CLOSED,
                AuctionTimePolicy.effectiveStatus(auction, END_AT.plusNanos(1))
        );
    }

    private Auction auctionEndingAt(LocalDateTime endAt) {
        return Auction.builder()
                .status(AuctionStatus.IN_PROGRESS)
                .endAt(endAt)
                .build();
    }
}
