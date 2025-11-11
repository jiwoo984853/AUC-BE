package com.mutsa.springboot_auction.domain.chat.dto;

import com.mutsa.springboot_auction.domain.chat.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long messageId;
    private Long senderId;
    private String senderName;
    private String messageContent;
    private LocalDateTime createdAt;

    public ChatMessageDto(ChatMessage message) {
        this.messageId = message.getMessageId();
        this.senderId = message.getUser().getId();
        this.senderName = message.getUser().getNickname();
        this.messageContent = message.getMessageContent();
        this.createdAt = message.getCreatedAt();
    }

}
