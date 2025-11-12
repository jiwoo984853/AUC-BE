package com.mutsa.springboot_auction.domain.chat.repository;

import com.mutsa.springboot_auction.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 메서드명을 chatRoom.chatroomId로 정확히 매칭
    List<ChatMessage> findByChatRoomChatroomIdOrderByCreatedAtAsc(Long chatroomId);
}
