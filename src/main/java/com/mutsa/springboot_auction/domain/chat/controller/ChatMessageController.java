package com.mutsa.springboot_auction.domain.chat.controller;


import com.mutsa.springboot_auction.domain.chat.dto.ChatMessageDto;
import com.mutsa.springboot_auction.domain.chat.service.ChatService;
import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatService chatService;

    /**
     * 메시지 전송
     * 클라이언트: /app/chat/rooms/{roomId}/send로 서버로 전송(pub)
     * 서버: /topic/chat/rooms/{roomId}로 브로드캐스트
     */
    @MessageMapping("/chat/rooms/{roomId}/send")
    @SendTo("/topic/chat/rooms/{roomId}")
    public ChatMessageDto sendMessage(
            @DestinationVariable Long roomId,
            ChatMessageDto messageDto,
            Principal principal
    ) {
        CustomOAuth2User user = (CustomOAuth2User) ((Authentication) principal).getPrincipal();
        ChatMessageDto savedMessage = chatService.saveMessage(
                roomId,
                user.getUser().getId(),
                messageDto.getMessageContent()
        );
        savedMessage.setClientMessageId(messageDto.getClientMessageId());
        return savedMessage;
    }

}
