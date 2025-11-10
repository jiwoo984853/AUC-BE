package com.mutsa.springboot_auction.domain.auction.entity;


import com.mutsa.springboot_auction.domain.auctionCategory.entity.AuctionCategory;
import com.mutsa.springboot_auction.domain.auctionImage.entity.AuctionImage;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Auction")
public class Auction extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer auctionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sellerId", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User winner;

    private String goodsName;
    private String description;
    private Integer startPrice;
    private Integer cuurentPrice;

    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    private String startAt;
    private String endAt;
    private String condition;
    private String incluedItems;
    private String year;
    private String location;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL)
    private List<AuctionCategory> categories;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuctionImage> images;
}

enum AuctionStatus {
    IN_PROGRESS, SOLD, CANCELLED
}


