package com.mutsa.springboot_auction.domain.auctionCategory.entity;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auctionCategory.AuctionCategoryId;
import com.mutsa.springboot_auction.domain.category.entity.Category;
import com.mutsa.springboot_auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auction_category")
public class AuctionCategory extends BaseTimeEntity {

    @EmbeddedId
    @Builder.Default
    private AuctionCategoryId id = new AuctionCategoryId();

    @MapsId("categoryId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @MapsId("auctionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;
}
