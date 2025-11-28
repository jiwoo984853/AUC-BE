package com.mutsa.springboot_auction.domain.auction.dto;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.entity.AuctionStatus;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class AuctionSimpleResponse {
    Long id;
    String title;
    String imageUrl;
    int currentPrice;
    LocalDateTime endAt;

    AuctionStatus status;

    private AuctionSimpleResponse(Long id, String title, String imageUrl, int currentPrice, LocalDateTime endAt, AuctionStatus status) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.currentPrice = currentPrice;
        this.endAt = endAt;
        this.status = status;
    }

    public static AuctionSimpleResponse from(Auction auction) {
        String thumbnailUrl = auction.getImages().isEmpty()
                ? null
                : auction.getImages().get(0).getImageUrl();

        return new AuctionSimpleResponse(
                auction.getAuctionId(),
                auction.getGoodsName(),
                thumbnailUrl,
                auction.getCurrentPrice(),
                auction.getEndAt(),
                auction.getStatus()
        );
    }
}
