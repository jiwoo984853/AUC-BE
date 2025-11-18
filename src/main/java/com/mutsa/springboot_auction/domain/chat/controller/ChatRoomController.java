package com.mutsa.springboot_auction.domain.chat.controller;

import com.mutsa.springboot_auction.domain.chat.dto.ChatRoomResponseDto;
import com.mutsa.springboot_auction.domain.chat.dto.MessageResponseDto;
import com.mutsa.springboot_auction.domain.chat.service.ChatService;
import com.mutsa.springboot_auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatRoomController {

    private final ChatService chatService;

    // 채팅방 생성 엔드포인트
    @PostMapping("/auctions/{auctionId}/chat")
    public ResponseEntity<ChatRoomResponseDto> createOrGetChatRoom(
            @PathVariable Long auctionId,
            @AuthenticationPrincipal(expression = "user") User currentUser
    ) {
        Long currentUserId = currentUser.getId();

        ChatRoomResponseDto chatRoom = chatService.createOrGetChatRoom(auctionId, currentUserId);

        return ResponseEntity.ok(chatRoom);
    }

    // 채팅방 메세지 조회 엔드포인트
    @GetMapping("/chat/rooms/{roomId}/messages")
    public ResponseEntity<List<MessageResponseDto>> getMessages(
            @PathVariable Long roomId,
            @AuthenticationPrincipal(expression = "user") User currentUser
    ) {
        Long currentUserId = currentUser.getId();

        List<MessageResponseDto> messages = chatService.getMessages(roomId, currentUserId);

        return ResponseEntity.ok(messages);
    }

    // 거래완료 처리
    @PostMapping("/chat/rooms/{roomId}/complete")
    public ResponseEntity<String> completeTransaction(
            @PathVariable Long roomId,
            @AuthenticationPrincipal(expression = "user") User currentUser
    ) {
        chatService.completeTransaction(roomId, currentUser.getId());
        return ResponseEntity.ok("거래가 완료되었습니다");
    }

    // 거래완료 처리 - 테스트용
    @PostMapping("/chat/rooms/{roomId}/complete/test")
    public ResponseEntity<String> completeTransactionTest(
            @PathVariable Long roomId,
            @RequestParam Long userId
    ) {
        chatService.completeTransaction(roomId, userId);
        return ResponseEntity.ok("거래가 완료되었습니다");
    }
}
