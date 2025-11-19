package com.mutsa.springboot_auction.domain.auctionImage.entity;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auction_image")
public class AuctionImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_image_id", nullable = false)
    private Long id;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auctionId", nullable = false)
    private Auction auction;

    public AuctionImage(String imageUrl, Auction auction) {
        this.imageUrl = imageUrl;
        this.auction = auction;
    }
}
