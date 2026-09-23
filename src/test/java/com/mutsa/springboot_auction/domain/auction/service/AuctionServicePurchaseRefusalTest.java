package com.mutsa.springboot_auction.domain.auction.service;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.entity.AuctionStatus;
import com.mutsa.springboot_auction.domain.auction.entity.PaymentStatus;
import com.mutsa.springboot_auction.domain.auction.repository.AuctionRepository;
import com.mutsa.springboot_auction.domain.auctionImage.repository.AuctionImageRepository;
import com.mutsa.springboot_auction.domain.bid.entity.Bid;
import com.mutsa.springboot_auction.domain.bid.entity.BidStatus;
import com.mutsa.springboot_auction.domain.bid.entity.DepositStatus;
import com.mutsa.springboot_auction.domain.bid.repositoy.BidRepository;
import com.mutsa.springboot_auction.domain.category.repository.CategoryRepository;
import com.mutsa.springboot_auction.domain.notification.service.NotificationService;
import com.mutsa.springboot_auction.domain.pointHistory.service.PointService;
import com.mutsa.springboot_auction.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuctionServicePurchaseRefusalTest {
    private AuctionRepository auctions;
    private BidRepository bids;
    private PointService points;
    private AuctionService service;

    @BeforeEach
    void setUp() {
        auctions = mock(AuctionRepository.class);
        bids = mock(BidRepository.class);
        points = mock(PointService.class);
        service = new AuctionService(
                auctions,
                mock(AuctionImageRepository.class),
                mock(CategoryRepository.class),
                bids,
                mock(NotificationService.class),
                points
        );
    }

    @Test
    void nonWinnerCannotRefusePurchase() {
        Auction auction = mock(Auction.class);
        User winner = mock(User.class);
        User requester = mock(User.class);
        when(winner.getId()).thenReturn(10L);
        when(requester.getId()).thenReturn(20L);
        when(auction.getWinner()).thenReturn(winner);
        when(auctions.findById(7L)).thenReturn(Optional.of(auction));

        assertThrows(IllegalArgumentException.class,
                () -> service.refuseToPurchase(7L, requester));

        verifyNoInteractions(bids, points);
        verify(auction, never()).setPaymentStatus(any());
    }

    @Test
    void winnerCanRefusePurchase() {
        Auction auction = mock(Auction.class);
        User winner = mock(User.class);
        User requester = mock(User.class);
        User seller = mock(User.class);
        Bid bid = mock(Bid.class);
        when(winner.getId()).thenReturn(10L);
        when(requester.getId()).thenReturn(10L);
        when(auction.getWinner()).thenReturn(winner);
        when(auction.getSeller()).thenReturn(seller);
        when(auctions.findById(7L)).thenReturn(Optional.of(auction));
        when(bids.findTopByAuction_AuctionIdAndUserIdOrderByCreatedAtDesc(7L, 10L))
                .thenReturn(Optional.of(bid));
        when(bid.getDepositAmount()).thenReturn(500);

        service.refuseToPurchase(7L, requester);

        verify(bid).setStatus(BidStatus.CANCELLED);
        verify(bid).setDepositStatus(DepositStatus.USED);
        verify(auction).setStatus(AuctionStatus.CLOSED);
        verify(auction).setPaymentStatus(PaymentStatus.COMPLETED);
        verify(seller).addPoint(500);
        verify(points).saveCancelBidHistory(seller, 500);
    }
}
