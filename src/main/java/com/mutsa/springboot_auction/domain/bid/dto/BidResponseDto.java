package com.mutsa.springboot_auction.domain.bid.dto;

import com.mutsa.springboot_auction.domain.bid.entity.BidStatus;
import com.mutsa.springboot_auction.domain.bid.entity.DepositStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BidResponseDto {

    private Long bidId;
    private Long userId;
    private Long auctionId;
    private Integer bidPrice;
    private Integer depositAmount;
    private BidStatus status;
    private DepositStatus depositStatus;
}
