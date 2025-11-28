package com.mutsa.springboot_auction.domain.bid.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WonItemResponseDto {
    private Long auctionId;
    private String goodsName;
    private Integer finalPrice;
    private String imageUrl;
    private LocalDateTime createAt;
}
