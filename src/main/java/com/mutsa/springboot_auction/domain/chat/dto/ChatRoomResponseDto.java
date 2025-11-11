package com.mutsa.springboot_auction.domain.chat.dto;

import com.mutsa.springboot_auction.domain.chat.entity.ChatRoom;
import com.mutsa.springboot_auction.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponseDto {
    private Long chatRoomId;
    private Long auctionId;
    private User seller;
    private User buyer;

    public ChatRoomResponseDto(ChatRoom room) {
        this.chatRoomId = room.getChatroomId();
        this.auctionId = room.getAuction().getAuctionId();
        this.seller = room.getSeller();
        this.buyer = room.getBuyer();
    }

}
