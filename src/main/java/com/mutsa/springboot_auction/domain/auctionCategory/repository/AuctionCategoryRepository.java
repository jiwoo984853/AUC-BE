package com.mutsa.springboot_auction.domain.auctionCategory.repository;

import com.mutsa.springboot_auction.domain.auctionCategory.entity.AuctionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionCategoryRepository extends JpaRepository<AuctionCategory, Long> {
}
