package com.mutsa.springboot_auction.domain.chat.repository;

import com.mutsa.springboot_auction.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomChatroomIdOrderByCreatedAtAsc(Long chatRoomId);
}
