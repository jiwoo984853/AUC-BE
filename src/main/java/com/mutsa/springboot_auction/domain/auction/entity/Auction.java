package com.mutsa.springboot_auction.domain.auction.entity;


import com.mutsa.springboot_auction.domain.auction.dto.AuctionRequest;
import com.mutsa.springboot_auction.domain.auctionCategory.entity.AuctionCategory;
import com.mutsa.springboot_auction.domain.auctionImage.entity.AuctionImage;
import com.mutsa.springboot_auction.domain.bid.entity.Bid;
import com.mutsa.springboot_auction.domain.user.entity.User;
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
    @Column(name = "auction_id", nullable = false)  // 명시적으로 추가
    private Long auctionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User winner;

    @Column(name = "goods_name")
    private String goodsName;

    private String description;

    @Column(name = "start_price")
    private Integer startPrice;

    @Column(name = "current_price")
    private Integer currentPrice;

    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    @Enumerated(EnumType.STRING)
    private TransactionMethod transactionMethod;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "item_condition")
    private String condition;

    @Column(name = "included_items")
    private String includedItems;

    private String manufactureYear;
    private String location;

    // 결제 완료하면 COMPLETED로 바뀜
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL)
    private List<AuctionCategory> categories;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuctionImage> images;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bid> bids;

    private Boolean hideBidPrice;

    public Auction(User seller, String goodsName, String description,
                   Integer startPrice, AuctionStatus status,
                   LocalDateTime startAt, LocalDateTime endAt,
                   String condition, TransactionMethod transactionMethod, String manufactureYear, String location,
                   Boolean hideBidPrice) {
        this.seller = seller;
        this.goodsName = goodsName;
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


