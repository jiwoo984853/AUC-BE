package com.mutsa.springboot_auction.domain.auctionCategory;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AuctionCategoryId {
    // 복합키 사용을 위한 클래스
    private Integer auctionId; // FK
    private Integer categoryId; // FK
}
