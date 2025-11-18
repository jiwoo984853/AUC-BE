package com.mutsa.springboot_auction.domain.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SwipeActionRequest {
    private Long auctionId;
    private String action;
}
