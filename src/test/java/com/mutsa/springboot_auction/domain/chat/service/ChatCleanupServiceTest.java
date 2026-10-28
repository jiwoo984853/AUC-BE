package com.mutsa.springboot_auction.domain.chat.service;

import com.mutsa.springboot_auction.domain.auction.entity.PaymentStatus;
import com.mutsa.springboot_auction.domain.chat.entity.ChatRoom;
import com.mutsa.springboot_auction.domain.chat.repository.ChatMessageRepository;
import com.mutsa.springboot_auction.domain.chat.repository.ChatRoomRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ChatCleanupServiceTest {
    @Test
    void messagesAreDeletedBeforeExpiredChatRooms() {
        ChatRoomRepository rooms = mock(ChatRoomRepository.class);
        ChatMessageRepository messages = mock(ChatMessageRepository.class);
        ChatRoom firstRoom = mock(ChatRoom.class);
        ChatRoom secondRoom = mock(ChatRoom.class);
        when(firstRoom.getChatroomId()).thenReturn(10L);
        when(secondRoom.getChatroomId()).thenReturn(20L);
        when(rooms.findAllByAuctionPaymentStatusAndAuctionPaymentCompletedAtBefore(
                eq(PaymentStatus.COMPLETED), any(LocalDateTime.class)))
                .thenReturn(List.of(firstRoom, secondRoom));

        new ChatCleanupService(rooms, messages).deleteExpiredChatRooms();

        var order = inOrder(messages, rooms);
        order.verify(messages).deleteAllByChatRoomIds(List.of(10L, 20L));
        order.verify(rooms).deleteAllByIdInBatch(List.of(10L, 20L));
    }

    @Test
    void nothingIsDeletedWhenNoExpiredRoomsExist() {
        ChatRoomRepository rooms = mock(ChatRoomRepository.class);
        ChatMessageRepository messages = mock(ChatMessageRepository.class);
        when(rooms.findAllByAuctionPaymentStatusAndAuctionPaymentCompletedAtBefore(
                eq(PaymentStatus.COMPLETED), any(LocalDateTime.class)))
                .thenReturn(List.of());

        new ChatCleanupService(rooms, messages).deleteExpiredChatRooms();

        verifyNoInteractions(messages);
        verify(rooms, never()).deleteAllByIdInBatch(anyList());
    }
}
