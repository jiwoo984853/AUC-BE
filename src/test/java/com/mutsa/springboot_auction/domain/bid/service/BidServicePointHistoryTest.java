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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class BidServicePointHistoryTest {
    @Test
    void bidIsRejectedAfterExactAuctionEndTimeEvenBeforeSchedulerClosesIt() {
        BidRepository bids = mock(BidRepository.class);
        AuctionRepository auctions = mock(AuctionRepository.class);
        UserRepository users = mock(UserRepository.class);
        PointService points = mock(PointService.class);
        NotificationService notifications = mock(NotificationService.class);
        BidService service = new BidService(bids, auctions, users, points, notifications);

        Auction auction = mock(Auction.class);
        User bidder = mock(User.class);
        when(auction.getStatus()).thenReturn(AuctionStatus.IN_PROGRESS);
        when(auction.getPaymentStatus()).thenReturn(PaymentStatus.PENDING);
        when(auction.getEndAt()).thenReturn(LocalDateTime.now().minusSeconds(1));
        when(auctions.findByIdForUpdate(7L)).thenReturn(Optional.of(auction));
        when(users.findById(42L)).thenReturn(Optional.of(bidder));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createBid(42L, new BidCreateRequestDto(7L, 200))
        );

        assertEquals("이미 종료된 경매입니다.", exception.getMessage());
        verify(bids, never()).save(any(Bid.class));
        verify(bidder, never()).subtractPoint(anyInt());
        verifyNoInteractions(points, notifications);
    }

    @Test
    void sellerCannotBidOnOwnAuction() {
        BidRepository bids = mock(BidRepository.class);
        AuctionRepository auctions = mock(AuctionRepository.class);
        UserRepository users = mock(UserRepository.class);
        PointService points = mock(PointService.class);
        NotificationService notifications = mock(NotificationService.class);
        BidService service = new BidService(bids, auctions, users, points, notifications);

        Auction auction = mock(Auction.class);
        User seller = mock(User.class);
        when(seller.getId()).thenReturn(42L);
        when(auction.getSeller()).thenReturn(seller);
        when(auctions.findByIdForUpdate(7L)).thenReturn(Optional.of(auction));
        when(users.findById(42L)).thenReturn(Optional.of(seller));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createBid(42L, new BidCreateRequestDto(7L, 200))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("본인이 등록한 경매에는 입찰할 수 없습니다.", exception.getReason());
        verify(bids, never()).save(any(Bid.class));
        verify(seller, never()).subtractPoint(anyInt());
        verifyNoInteractions(points, notifications);
    }

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
        when(bidder.getPoint()).thenReturn(20);
        when(previousBidder.getId()).thenReturn(99L);
        when(previousBid.getUser()).thenReturn(previousBidder);
        when(previousBid.getDepositAmount()).thenReturn(15);
        when(auctions.findByIdForUpdate(7L)).thenReturn(Optional.of(auction));
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

    @Test
    void bidIsRejectedWhenAvailablePointsAreLessThanDeposit() {
        BidRepository bids = mock(BidRepository.class);
        AuctionRepository auctions = mock(AuctionRepository.class);
        UserRepository users = mock(UserRepository.class);
        PointService points = mock(PointService.class);
        NotificationService notifications = mock(NotificationService.class);
        BidService service = new BidService(bids, auctions, users, points, notifications);

        Auction auction = mock(Auction.class);
        User bidder = mock(User.class);
        when(auction.getStatus()).thenReturn(AuctionStatus.IN_PROGRESS);
        when(auction.getPaymentStatus()).thenReturn(PaymentStatus.PENDING);
        when(auction.getCurrentPrice()).thenReturn(100);
        when(bidder.getId()).thenReturn(42L);
        when(bidder.getPoint()).thenReturn(19);
        when(auctions.findByIdForUpdate(7L)).thenReturn(Optional.of(auction));
        when(users.findById(42L)).thenReturn(Optional.of(bidder));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createBid(42L, new BidCreateRequestDto(7L, 200))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "입찰 보증금에 필요한 포인트가 부족합니다. (필요 보증금: 20, 보유 포인트: 19)",
                exception.getReason()
        );
        verify(bidder, never()).subtractPoint(anyInt());
        verify(bids, never()).save(any(Bid.class));
        verifyNoInteractions(points, notifications);
    }
}
