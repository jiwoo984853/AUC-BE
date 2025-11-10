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
@Table(name = "AuctionCategory")
public class AuctionCategory extends BaseTimeEntity {

    @EmbeddedId
    private AuctionCategoryId id;

    @MapsId("categoryId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryId")
    private Category category;

    @MapsId("auctionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auctionId")
    private Auction auction;
}
