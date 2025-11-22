package com.mutsa.springboot_auction.domain.bid.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BidListResponseDto {
    private int count;
    private List<BidResponseDto> bids;
}

