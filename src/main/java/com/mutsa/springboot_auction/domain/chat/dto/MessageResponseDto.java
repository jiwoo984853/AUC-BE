package com.mutsa.springboot_auction.domain.chat.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDto {
    private Long messageId;
    private Long senderId;
    private String senderName;
    private String messageContent;
    private LocalDateTime sendAt;
    private boolean isMyMessage; // 내가 보낸 메세지인지
    private Boolean isRead;
}
