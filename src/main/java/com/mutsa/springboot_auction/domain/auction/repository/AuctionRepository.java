package com.mutsa.springboot_auction.domain.auction.repository;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    List<Auction> findByEndAtAfterOrderByAuctionIdDesc(LocalDateTime now);

    List<Auction> findBySeller(User seller);

    @Query("select a from Auction a " +
            "where a.status = 'IN_PROGRESS' and a.endAt <= :now")
    List<Auction> findAuctionsToClose(@Param("now") LocalDateTime now);

}
