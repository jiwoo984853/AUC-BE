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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class AuctionServiceDeleteTest {

    @Test
    void deletingActiveAuctionRefundsHeldDepositAndNotifiesEveryBidder() {
        AuctionRepository auctions = mock(AuctionRepository.class);
        AuctionImageRepository images = mock(AuctionImageRepository.class);
        CategoryRepository categories = mock(CategoryRepository.class);
        BidRepository bids = mock(BidRepository.class);
        NotificationService notifications = mock(NotificationService.class);
        PointService points = mock(PointService.class);
        AuctionService service = new AuctionService(auctions, images, categories, bids, notifications, points);

        User seller = mock(User.class);
        User highestBidder = mock(User.class);
        User previousBidder = mock(User.class);
        Auction auction = mock(Auction.class);
        Bid heldBid = mock(Bid.class);
        Bid refundedBid = mock(Bid.class);

        when(seller.getId()).thenReturn(1L);
        when(highestBidder.getId()).thenReturn(2L);
        when(previousBidder.getId()).thenReturn(3L);
        when(auction.getSeller()).thenReturn(seller);
        when(auction.getPaymentStatus()).thenReturn(PaymentStatus.PENDING);
        when(auction.getStatus()).thenReturn(AuctionStatus.IN_PROGRESS);
        when(auction.getAuctionId()).thenReturn(7L);
        when(auction.getGoodsName()).thenReturn("테스트 경매");
        when(heldBid.getUser()).thenReturn(highestBidder);
        when(heldBid.getDepositStatus()).thenReturn(DepositStatus.HELD);
        when(heldBid.getDepositAmount()).thenReturn(1000);
        when(heldBid.getStatus()).thenReturn(BidStatus.PENDING);
        when(refundedBid.getUser()).thenReturn(previousBidder);
        when(refundedBid.getDepositStatus()).thenReturn(DepositStatus.REFUNDED);
        when(refundedBid.getStatus()).thenReturn(BidStatus.FAILED);
        when(auctions.findByIdForUpdate(7L)).thenReturn(Optional.of(auction));
        when(bids.findAllByAuctionOrderByCreatedAtDesc(auction))
                .thenReturn(List.of(heldBid, refundedBid));

        service.deleteAuction(7L, seller);

        verify(highestBidder).addPoint(1000);
        verify(points).saveRefundHistory(highestBidder, 1000);
        verify(heldBid).setDepositStatus(DepositStatus.REFUNDED);
        verify(heldBid).setStatus(BidStatus.CANCELLED);
        verify(previousBidder, never()).addPoint(anyInt());
        verify(notifications).sendAuctionCancelledNotification(2L, "테스트 경매", 7L);
        verify(notifications).sendAuctionCancelledNotification(3L, "테스트 경매", 7L);
        verify(auction).setStatus(AuctionStatus.CANCELLED);
        verify(auction).setWinner(null);
        verify(auction).softDelete();
    }

    @Test
    void deletingCompletedAuctionOnlyHidesItWithoutRefundOrNotification() {
        AuctionRepository auctions = mock(AuctionRepository.class);
        AuctionImageRepository images = mock(AuctionImageRepository.class);
        CategoryRepository categories = mock(CategoryRepository.class);
        BidRepository bids = mock(BidRepository.class);
        NotificationService notifications = mock(NotificationService.class);
        PointService points = mock(PointService.class);
        AuctionService service = new AuctionService(auctions, images, categories, bids, notifications, points);

        User seller = mock(User.class);
        Auction auction = mock(Auction.class);
        when(seller.getId()).thenReturn(1L);
        when(auction.getSeller()).thenReturn(seller);
        when(auction.getPaymentStatus()).thenReturn(PaymentStatus.COMPLETED);
        when(auctions.findByIdForUpdate(7L)).thenReturn(Optional.of(auction));

        service.deleteAuction(7L, seller);

        verify(auction).softDelete();
        verifyNoInteractions(bids, notifications, points);
    }
}
