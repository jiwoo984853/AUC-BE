package com.mutsa.springboot_auction.domain.bid.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BidItemResponseDto {

    private Long auctionId;
    private String goodsName;
    private Integer currentPrice;
    private String imageUrl;
    private String status;
}
