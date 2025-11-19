package com.mutsa.springboot_auction.domain.auction.dto;

import com.mutsa.springboot_auction.domain.auction.entity.TransactionMethod;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuctionRequest {

    @NotBlank(message = "상품명은 필수입니다.")
    private String goodsName;

    @NotBlank(message = "상품 설명은 필수입니다.")
    private String description;

    @NotNull(message = "시작가는 필수입니다.")
    @Positive(message = "시작가는 0보다 커야 합니다.")
    private Integer startPrice;

    @NotNull(message = "경매 시작일은 필수입니다.")
    private LocalDateTime startAt;

    @NotNull(message = "경매 종료일은 필수입니다.")
    @Future(message = "종료일은 미래여야 합니다.")
    private LocalDateTime endAt;

    private String condition;

    @NotNull(message = "거래 방식은 필수입니다.")
    private TransactionMethod transactionMethod;

    private String manufactureYear;
    private String location;

    private List<String> imageUrls;

    private List<Integer> categoryIds;

    @NotNull(message = "입찰가 숨김 여부를 입력해주세요.")
    private Boolean hideBidPrice;
}

