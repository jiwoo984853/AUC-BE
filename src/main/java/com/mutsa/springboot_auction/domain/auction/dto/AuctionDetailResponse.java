package com.mutsa.springboot_auction.domain.auction.dto;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.entity.AuctionStatus;
import com.mutsa.springboot_auction.domain.auction.entity.TransactionMethod;
import com.mutsa.springboot_auction.domain.auctionImage.entity.AuctionImage;
import com.mutsa.springboot_auction.domain.user.dto.UserResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AuctionDetailResponse {
    private Long auctionId;
    private UserResponse seller;
    private String goodsName;
    private String description;
    private Integer startPrice;
    private List<String> imageUrl;
    private AuctionStatus status;
    private TransactionMethod transactionMethod;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String condition;
    private String manufactureYear;
    private String location;
    private Boolean hideBidPrice;
    private UserResponse winner;
    private int totalNumOfBid;
    public static AuctionDetailResponse from(Auction auction) {
        List<String> imageUrls = auction.getImages()
                .stream()
                .map(AuctionImage::getImageUrl)
                .toList();

        return new AuctionDetailResponse(
                auction.getAuctionId(),
                UserResponse.from(auction.getSeller()),
                auction.getGoodsName(),
                auction.getDescription(),
                auction.getStartPrice(),
                imageUrls,
                auction.getStatus(),
                auction.getTransactionMethod(),
                auction.getStartAt(),
                auction.getEndAt(),
                auction.getCondition(),
                auction.getManufactureYear(),
                auction.getLocation(),
                auction.getHideBidPrice(),
                UserResponse.from(auction.getWinner()),
                auction.getBids().size()
        );
    }

}
