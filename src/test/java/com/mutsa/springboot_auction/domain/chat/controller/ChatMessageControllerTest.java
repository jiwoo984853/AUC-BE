package com.mutsa.springboot_auction.domain.chat.controller;

import com.mutsa.springboot_auction.domain.chat.dto.ChatMessageDto;
import com.mutsa.springboot_auction.domain.chat.service.ChatService;
import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import com.mutsa.springboot_auction.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Map;

import static org.mockito.Mockito.*;

class ChatMessageControllerTest {
    @Test
    void senderIdInMessageIsIgnored() {
        ChatService chatService = mock(ChatService.class);
        ChatMessageController controller = new ChatMessageController(chatService);
        User user = mock(User.class);
        when(user.getId()).thenReturn(42L);
        ChatMessageDto message = new ChatMessageDto();
        message.setSenderId(999L);
        message.setMessageContent("hello");
        message.setClientMessageId("client-123");
        ChatMessageDto saved = new ChatMessageDto();
        when(chatService.saveMessage(7L, 42L, "hello")).thenReturn(saved);

        ChatMessageDto result = controller.sendMessage(7L, message, new UsernamePasswordAuthenticationToken(
                new CustomOAuth2User(user, Map.of()), null, java.util.List.of()));

        verify(chatService).saveMessage(7L, 42L, "hello");
        org.junit.jupiter.api.Assertions.assertEquals("client-123", result.getClientMessageId());
    }
}
