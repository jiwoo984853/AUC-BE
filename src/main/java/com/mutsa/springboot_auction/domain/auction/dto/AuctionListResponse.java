package com.mutsa.springboot_auction.domain.auction.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuctionListResponse {
    List<AuctionSimpleResponse> auctions;

    public static AuctionListResponse of(List<AuctionSimpleResponse> auctionSimpleResponses) {
        return new AuctionListResponse(auctionSimpleResponses);
    }
}
