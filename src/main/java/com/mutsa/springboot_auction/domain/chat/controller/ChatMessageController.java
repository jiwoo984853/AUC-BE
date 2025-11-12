package com.mutsa.springboot_auction.domain.chat.controller;


import com.mutsa.springboot_auction.domain.chat.dto.ChatMessageDto;
import com.mutsa.springboot_auction.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatService chatService;

    /**
     * 메시지 전송
     * 클라이언트: /app/chat/rooms/{roomId}/send로 전송(pub)
     * 서버: /topic/chat/rooms/{roomId}로 브로드캐스트
     */
    @MessageMapping("/chat/rooms/{roomId}/send")
    @SendTo("/topic/chat/rooms/{roomId}")
    public ChatMessageDto sendMessage(
            @DestinationVariable Long roomId,
            ChatMessageDto messageDto
    ) {
        return chatService.saveMessage(
                roomId,
                messageDto.getSenderId(),
                messageDto.getMessageContent()
        );
    }

}
