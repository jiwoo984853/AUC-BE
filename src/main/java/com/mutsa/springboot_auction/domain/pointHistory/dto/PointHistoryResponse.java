package com.mutsa.springboot_auction.domain.pointHistory.dto;

import com.mutsa.springboot_auction.domain.pointHistory.entity.HistoryType;
import com.mutsa.springboot_auction.domain.pointHistory.entity.PointHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryResponse {
    private Long historyId;
    private Integer changeAmount;
    private HistoryType type;

    public static PointHistoryResponse of(PointHistory history) {
        return PointHistoryResponse.builder()
                .historyId(history.getHistoryId())
                .changeAmount(history.getChangeAmount())
                .type(history.getType())
                .build();
    }
}
