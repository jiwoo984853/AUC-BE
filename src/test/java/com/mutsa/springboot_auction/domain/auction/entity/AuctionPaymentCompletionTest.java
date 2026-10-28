package com.mutsa.springboot_auction.domain.auction.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuctionPaymentCompletionTest {
    @Test
    void completionTimeIsRecordedOnlyOnce() {
        Auction auction = Auction.builder()
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        auction.completePayment();
        LocalDateTime firstCompletedAt = auction.getPaymentCompletedAt();
        auction.completePayment();

        assertEquals(PaymentStatus.COMPLETED, auction.getPaymentStatus());
        assertNotNull(firstCompletedAt);
        assertEquals(firstCompletedAt, auction.getPaymentCompletedAt());
    }
}
