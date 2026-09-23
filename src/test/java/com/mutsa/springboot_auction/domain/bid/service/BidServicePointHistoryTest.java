package com.mutsa.springboot_auction.domain.bid.service;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.entity.AuctionStatus;
import com.mutsa.springboot_auction.domain.auction.entity.PaymentStatus;
import com.mutsa.springboot_auction.domain.auction.repository.AuctionRepository;
import com.mutsa.springboot_auction.domain.bid.dto.BidCreateRequestDto;
import com.mutsa.springboot_auction.domain.bid.entity.Bid;
import com.mutsa.springboot_auction.domain.bid.entity.BidStatus;
import com.mutsa.springboot_auction.domain.bid.entity.DepositStatus;
import com.mutsa.springboot_auction.domain.bid.repositoy.BidRepository;
import com.mutsa.springboot_auction.domain.notification.service.NotificationService;
import com.mutsa.springboot_auction.domain.pointHistory.service.PointService;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BidServicePointHistoryTest {
    @Test
    void newDepositAndPreviousBidRefundUseDifferentHistoryTypes() {
        BidRepository bids = mock(BidRepository.class);
        AuctionRepository auctions = mock(AuctionRepository.class);
        UserRepository users = mock(UserRepository.class);
        PointService points = mock(PointService.class);
        NotificationService notifications = mock(NotificationService.class);
        BidService service = new BidService(bids, auctions, users, points, notifications);

        Auction auction = mock(Auction.class);
        User bidder = mock(User.class);
        User previousBidder = mock(User.class);
        Bid previousBid = mock(Bid.class);
        when(auction.getStatus()).thenReturn(AuctionStatus.IN_PROGRESS);
        when(auction.getPaymentStatus()).thenReturn(PaymentStatus.PENDING);
        when(auction.getCurrentPrice()).thenReturn(100);
        when(auction.getAuctionId()).thenReturn(7L);
        when(auction.getGoodsName()).thenReturn("상품");
        when(bidder.getId()).thenReturn(42L);
        when(bidder.getPoint()).thenReturn(1_000);
        when(previousBidder.getId()).thenReturn(99L);
        when(previousBid.getUser()).thenReturn(previousBidder);
        when(previousBid.getDepositAmount()).thenReturn(15);
        when(auctions.findById(7L)).thenReturn(Optional.of(auction));
        when(users.findById(42L)).thenReturn(Optional.of(bidder));
        when(bids.findFirstByAuctionOrderByBidPriceDesc(auction)).thenReturn(Optional.of(previousBid));
        when(bids.save(any(Bid.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createBid(42L, new BidCreateRequestDto(7L, 200));

        verify(previousBidder).addPoint(15);
        verify(points).saveRefundHistory(previousBidder, 15);
        verify(previousBid).setDepositStatus(DepositStatus.REFUNDED);
        verify(previousBid).setStatus(BidStatus.FAILED);
        verify(bidder).subtractPoint(20);
        verify(points).saveDepositHistory(bidder, 20);
        verify(points, never()).saveRefundHistory(bidder, 20);
    }
}
