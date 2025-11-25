package com.mutsa.springboot_auction.domain.pointHistory.entity;

public enum HistoryType {
    // 임의로 타입 이름 만들었으니 수정해서 사용
    CHARGE, // 포인트 충전
    WITHDRAW, // 출금
    BID_DEPOSIT, // 입찰 보증금
    REFUND, // 보증금 환불
    PURCHASE, // 구매 (낙찰가 90% 지불)
    SALE, // 판매 (낙찰가 100% 수령)

    CANCELD_BID//구매 취소로 보증금을 Seller에게
}