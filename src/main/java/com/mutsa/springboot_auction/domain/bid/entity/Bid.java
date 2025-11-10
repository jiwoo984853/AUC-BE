package com.mutsa.springboot_auction.domain.bid.entity;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "Bid")
public class Bid extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bidId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auctionId", nullable = false)
    private Auction auction;

    @Enumerated(EnumType.STRING)
    private BidStatus status;

    @Enumerated(EnumType.STRING)
    private DepositStatus depositStatus;

    private String depositAmount;
    private Integer basePrice;
    private Integer bidPrice;
}

enum BidStatus {
    PENDING, SUCCESS, FAIELD
}

enum DepositStatus {
    HELD, REFUNDED, USED
}
