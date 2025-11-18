package com.mutsa.springboot_auction.domain.auction.repository;

import com.mutsa.springboot_auction.domain.auction.entity.UserAuctionState;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuctionStateRepository extends JpaRepository<UserAuctionState,Long> {
    Optional<UserAuctionState> findByUserIdAndAuctionId(Long userId, Long auctionId);
}
