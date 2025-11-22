package com.mutsa.springboot_auction.domain.bid.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MyAuctionSummaryResponseDto {
    private List<WonItemResponseDto> wonItems;
    private List<BidItemResponseDto> bidItems;
}
