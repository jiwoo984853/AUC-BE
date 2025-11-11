package com.mutsa.springboot_auction.domain.category.entity;

import com.mutsa.springboot_auction.domain.auctionCategory.entity.AuctionCategory;
import com.mutsa.springboot_auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "category")
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "category_name")
    private String categoryName;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<AuctionCategory> auctions;
}
