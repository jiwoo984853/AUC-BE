package com.mutsa.springboot_auction.domain.bid.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BidCreateRequestDto {

    @NotNull
    private Long auctionId;

    @NotNull
    private Long userId;

    @NotNull
    @Min(1)
    private Integer bidPrice;

}
