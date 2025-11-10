package com.mutsa.springboot_auction.domain.pointHistory.entity;

import com.mutsa.springboot_auction.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PointHistory")
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer historyId;

    private Integer changeAmount;

    @Enumerated(EnumType.STRING)
    private HistoryType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;
}

enum HistoryType {
    // 임의로 타입 이름 만들었으니 수정해서 사용
    CHARGE, WITHDRAW, BID_DEPOSIT
}

