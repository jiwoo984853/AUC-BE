package com.mutsa.springboot_auction.domain.auction.entity;


import com.mutsa.springboot_auction.domain.auctionCategory.entity.AuctionCategory;
import com.mutsa.springboot_auction.domain.auctionImage.entity.AuctionImage;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auction")
public class Auction extends BaseTimeEntity {
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

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "item_condition")
    private String condition;

    @Column(name = "included_items")
    private String includedItems;

    private String year;
    private String location;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL)
    private List<AuctionCategory> categories;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuctionImage> images;
}


