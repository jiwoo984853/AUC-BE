package com.mutsa.springboot_auction.domain.bid.repositoy;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.bid.entity.Bid;
import com.mutsa.springboot_auction.domain.bid.entity.BidStatus;
import com.mutsa.springboot_auction.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {

    Optional<Bid> findTopByAuctionOrderByBidPriceDesc(Auction auction);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Bid> findFirstByAuctionOrderByBidPriceDesc(@Param("auction")Auction auction);

    @Query("SELECT COUNT(DISTINCT b.user) FROM Bid b WHERE b.auction.auctionId = :auctionId")
    long countUniqueBidders(Long auctionId);

    long countByAuctionAndUserAndStatus(Auction auction, User user, BidStatus status);

    List<Bid> findAllByAuctionOrderByCreatedAtDesc(Auction auction);

    List<Bid> findAllByUserOrderByCreatedAtDesc(User user);
}
