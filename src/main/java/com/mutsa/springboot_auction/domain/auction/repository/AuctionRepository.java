package com.mutsa.springboot_auction.domain.auction.repository;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    List<Auction> findByEndAtAfterOrderByAuctionIdDesc(LocalDateTime now);

}
