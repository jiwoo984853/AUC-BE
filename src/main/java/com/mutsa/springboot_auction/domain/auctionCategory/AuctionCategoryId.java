package com.mutsa.springboot_auction.domain.auctionCategory;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AuctionCategoryId {
    // 복합키 사용을 위한 클래스
    private Long auctionId; // FK
    private Long categoryId; // FK
}
