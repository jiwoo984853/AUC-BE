package com.mutsa.springboot_auction.domain.auction.dto;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import java.time.LocalDateTime;

public class AuctionDto {
    Long id;
    String title;
    String imageUrl;
    int currentPrice;
    LocalDateTime endAt;

    private AuctionDto(Long id, String title, String imageUrl, int currentPrice, LocalDateTime endAt) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.currentPrice = currentPrice;
        this.endAt = endAt;
    }

    public static AuctionDto from(Auction auction) {
        return new AuctionDto(
                auction.getAuctionId(),
                auction.getGoodsName(),
                auction.getImages().get(0).getImageUrl(),
                auction.getCurrentPrice(),
                auction.getEndAt()
        );
    }
}
