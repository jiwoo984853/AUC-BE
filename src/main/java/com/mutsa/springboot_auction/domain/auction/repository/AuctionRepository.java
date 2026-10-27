package com.mutsa.springboot_auction.domain.auction.repository;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    List<Auction> findByDeletedAtIsNullAndEndAtAfterOrderByAuctionIdDesc(LocalDateTime now);

    List<Auction> findBySellerAndDeletedAtIsNull(User seller);

    List<Auction> findAllByAuctionIdInAndDeletedAtIsNull(List<Long> auctionIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Auction a where a.auctionId = :auctionId")
    Optional<Auction> findByIdForUpdate(@Param("auctionId") Long auctionId);

    @Query("select a from Auction a " +
            "where a.status = 'IN_PROGRESS' and a.endAt <= :now and a.deletedAt is null")
    List<Auction> findAuctionsToClose(@Param("now") LocalDateTime now);

}
