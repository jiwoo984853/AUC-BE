package com.mutsa.springboot_auction.domain.chat.dto;

import com.mutsa.springboot_auction.domain.chat.entity.ChatRoom;
import com.mutsa.springboot_auction.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponseDto {
    private Long chatRoomId;
    private Long auctionId;

    // User 엔티티 대신 필요한 필드만 포함
    private Long sellerId;
    private String sellerNickname;

    private Long buyerId;
    private String buyerNickname;

}

