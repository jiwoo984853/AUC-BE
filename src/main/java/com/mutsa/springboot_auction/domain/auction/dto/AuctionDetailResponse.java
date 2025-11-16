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
    private Long id;
    private String title;
    private String description;
    private List<String> image_url;
    private AuctionStatus status;
    private LocalDateTime ends_at;

    private UserResponse seller;

    private int current_price;

    private String location;
    private String condition;
    private TransactionMethod transactionMethod;
    private String manufactureYear;
    public static AuctionDetailResponse from(Auction auction) {
        List<String> imageUrls = auction.getImages().stream().map(AuctionImage::getImageUrl).toList();

        return new AuctionDetailResponse(auction.getAuctionId(), auction.getGoodsName(), auction.getDescription(),
                imageUrls, auction.getStatus(),
                auction.getEndAt(), UserResponse.from(auction.getSeller()), auction.getCurrentPrice(),
                auction.getLocation(), auction.getCondition(), auction.getTransactionMethod(),
                auction.getManufactureYear());
    }

}
