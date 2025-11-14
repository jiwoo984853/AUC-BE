package com.mutsa.springboot_auction.domain.auction.entity;


import com.mutsa.springboot_auction.domain.auction.dto.AuctionRequest;
import com.mutsa.springboot_auction.domain.auctionCategory.entity.AuctionCategory;
import com.mutsa.springboot_auction.domain.auctionImage.entity.AuctionImage;
import com.mutsa.springboot_auction.domain.bid.entity.Bid;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@Table(name = "auction")
@Getter
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;
    private String itemName;
    private String description;
    private Integer startPrice;
    @Enumerated(EnumType.STRING)
    private AuctionStatus status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private String condition;

    @Enumerated(EnumType.STRING)
    private TransactionMethod transactionMethod;
    private String manufactureYear;
    private String location;

    private Boolean hideBidPrice;

    public Auction(User seller, String itemName, String description,
                   Integer startPrice, AuctionStatus status,
                   LocalDateTime startAt, LocalDateTime endAt,
                   String condition, TransactionMethod transactionMethod, String manufactureYear, String location,
                   Boolean hideBidPrice) {
        this.seller = seller;
        this.itemName = itemName;
        this.description = description;
        this.startPrice = startPrice;
        this.status = status;
        this.startAt = startAt;
        this.endAt = endAt;
        this.condition = condition;
        this.transactionMethod = transactionMethod;
        this.manufactureYear = manufactureYear;
        this.location = location;
        this.hideBidPrice = hideBidPrice;
    }

    public static Auction of(AuctionRequest auctionRequest, User seller) {
        return new Auction(seller, auctionRequest.getItemName(), auctionRequest.getDescription(),
                auctionRequest.getStartPrice(), AuctionStatus.IN_PROGRESS, auctionRequest.getStartAt(),
                auctionRequest.getEndAt(), auctionRequest.getCondition(), auctionRequest.getTransactionMethod(),
                auctionRequest.getManufactureYear(), auctionRequest.getLocation(), auctionRequest.getHideBidPrice());
    }
}


