package com.mutsa.springboot_auction.domain.auction.repository;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
}
