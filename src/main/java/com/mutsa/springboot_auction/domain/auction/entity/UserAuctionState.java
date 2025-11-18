package com.mutsa.springboot_auction.domain.auction.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "auction_id"})
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class UserAuctionState {
    @Id
    @GeneratedValue
    private Long id;

    private Long userId;

    private Long auctionId;

    @Enumerated(EnumType.STRING)
    private ItemState state;

    private LocalDateTime lastActionAt;
    private int holdCount;

    public static UserAuctionState create(Long userId, Long auctionId, ItemState itemState) {
        UserAuctionState s = new UserAuctionState();
        s.userId = userId;
        s.auctionId = auctionId;
        s.state = itemState;
        s.lastActionAt = LocalDateTime.now();
        s.holdCount = 0;
        return s;
    }
}
