package com.mutsa.springboot_auction.domain.search.repository;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;

public interface SearchRepository extends JpaRepository<Auction, Long> {

    @Query("select a from Auction a " +
            "join a.categories ac " +
            "where ac.id.categoryId = :categoryId")
    public List<Auction> getAuctionsByCategory(@Param("categoryId") Long categoryId);


    @Query("select a from Auction a " +
            "join a.categories ac " +
            "where ac.id.categoryId = :categoryId and replace(a.goodsName, ' ', '') like %:keyword%")
    public List<Auction> searchInCategory(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword
    );

    @Query("select a from Auction a " +
            "where replace(a.goodsName, ' ', '') like %:keyword%")
    public List<Auction> searchAllAuctions(@Param("keyword") String keyword);
}
