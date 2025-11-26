package com.mutsa.springboot_auction.domain.auction.dto;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.entity.AuctionStatus;
import com.mutsa.springboot_auction.domain.auction.entity.TransactionMethod;
import com.mutsa.springboot_auction.domain.auctionCategory.entity.AuctionCategory;
import com.mutsa.springboot_auction.domain.auctionImage.entity.AuctionImage;
import com.mutsa.springboot_auction.domain.category.dto.CategoryResponse;
import com.mutsa.springboot_auction.domain.category.entity.Category;
import com.mutsa.springboot_auction.domain.user.dto.UserResponse;
import com.mutsa.springboot_auction.domain.user.entity.User;
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
    private Integer currentPrice;
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
    private long totalNumOfBidder;
    private List<CategoryResponse> categories;
    private Integer myBidPrice;
    public static AuctionDetailResponse from(Auction auction, long totalNumOfBidder, Integer myBidPrice) {
        List<String> imageUrls = auction.getImages()
                .stream()
                .map(AuctionImage::getImageUrl)
                .toList();
        List<AuctionCategory> auctionCategories = auction.getCategories();
        List<CategoryResponse> categoryResponses = auctionCategories.stream().map(AuctionCategory::getCategory)
                .map(CategoryResponse::from).toList();
        Integer currentPrice = auction.getCurrentPrice() != null ? auction.getCurrentPrice() : 0;

        UserResponse winnerResponse = null;
        if(auction.getWinner() != null) {
            winnerResponse = UserResponse.from(auction.getWinner());
        }

        return new AuctionDetailResponse(
                auction.getAuctionId(),
                UserResponse.from(auction.getSeller()),
                auction.getGoodsName(),
                auction.getDescription(),
                auction.getStartPrice(),
                currentPrice,
                imageUrls,
                auction.getStatus(),
                auction.getTransactionMethod(),
                auction.getStartAt(),
                auction.getEndAt(),
                auction.getCondition(),
                auction.getManufactureYear(),
                auction.getLocation(),
                auction.getHideBidPrice(),
                winnerResponse,
                totalNumOfBidder,
                categoryResponses,
                myBidPrice
        );
    }
}
