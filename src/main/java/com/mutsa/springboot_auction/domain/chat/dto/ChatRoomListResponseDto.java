package com.mutsa.springboot_auction.domain.chat.dto;

import com.mutsa.springboot_auction.domain.auction.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomListResponseDto {
    private Long chatRoomId;
    private Long auctionId;
    private String goodsName;
    private String imageUrl;
    private Integer finalPrice;
    private PaymentStatus paymentStatus;
    private Long sellerId;
    private String sellerNickname;
    private Long buyerId;
    private String buyerNickname;
    private String opponentNickname;
    private String opponentProfileImageUrl;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
}
