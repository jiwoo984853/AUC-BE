package com.mutsa.springboot_auction.domain.auction.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DeckResponse {
    List<AuctionSimpleResponse> auctions;

    public static DeckResponse of(List<AuctionSimpleResponse> auctionSimpleResponses) {
        return new DeckResponse(auctionSimpleResponses);
    }
}
