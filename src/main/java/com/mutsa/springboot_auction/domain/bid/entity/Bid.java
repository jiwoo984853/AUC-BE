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
    @Column(name = "bid_id")
    private Long bidId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @Enumerated(EnumType.STRING)
    @Column(name = "bid_status")
    private BidStatus status;

    @Enumerated(EnumType.STRING)
    private DepositStatus depositStatus;

    private Integer depositAmount;
    private Integer bidPrice;
}

enum BidStatus {
    PENDING, SUCCESS, FAILED
}

enum DepositStatus {
    HELD, REFUNDED, USED
}
