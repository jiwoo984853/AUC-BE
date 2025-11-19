package com.mutsa.springboot_auction.domain.auctionImage.repository;

import com.mutsa.springboot_auction.domain.auctionImage.entity.AuctionImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionImageRepository extends JpaRepository<AuctionImage, Long> {
}
