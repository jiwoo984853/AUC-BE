package com.mutsa.springboot_auction.domain.search.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private Long auctionId;
    private String goodsName;
    private String status;
    private String remainingTime;
    private Integer currentPrice;
    private String imageUrl;
    private String condition;
    private String year;
    private Long categoryId;
}
