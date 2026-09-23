package com.mutsa.springboot_auction.domain.chat.service;

import com.mutsa.springboot_auction.domain.auction.repository.AuctionRepository;
import com.mutsa.springboot_auction.domain.bid.repositoy.BidRepository;
import com.mutsa.springboot_auction.domain.chat.entity.ChatRoom;
import com.mutsa.springboot_auction.domain.chat.repository.ChatMessageRepository;
import com.mutsa.springboot_auction.domain.chat.repository.ChatRoomRepository;
import com.mutsa.springboot_auction.domain.pointHistory.repository.PointHistoryRepository;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ChatServiceAuthorizationTest {
    @Test
    void nonParticipantCannotSaveMessage() {
        ChatRoomRepository rooms = mock(ChatRoomRepository.class);
        ChatMessageRepository messages = mock(ChatMessageRepository.class);
        User buyer = mock(User.class);
        User seller = mock(User.class);
        when(buyer.getId()).thenReturn(1L);
        when(seller.getId()).thenReturn(2L);
        ChatRoom room = mock(ChatRoom.class);
        when(room.getBuyer()).thenReturn(buyer);
        when(room.getSeller()).thenReturn(seller);
        when(rooms.findById(7L)).thenReturn(Optional.of(room));
        ChatService service = new ChatService(
                mock(AuctionRepository.class), rooms, messages, mock(UserRepository.class),
                mock(PointHistoryRepository.class), mock(BidRepository.class));

        assertThrows(AccessDeniedException.class, () -> service.saveMessage(7L, 42L, "hello"));
        verifyNoInteractions(messages);
    }
}
