package com.mutsa.springboot_auction.domain.chat.service;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.entity.AuctionStatus;
import com.mutsa.springboot_auction.domain.auction.repository.AuctionRepository;
import com.mutsa.springboot_auction.domain.chat.dto.ChatMessageDto;
import com.mutsa.springboot_auction.domain.chat.dto.ChatRoomResponseDto;
import com.mutsa.springboot_auction.domain.chat.dto.MessageResponseDto;
import com.mutsa.springboot_auction.domain.chat.entity.ChatMessage;
import com.mutsa.springboot_auction.domain.chat.entity.ChatRoom;
import com.mutsa.springboot_auction.domain.chat.repository.ChatMessageRepository;
import com.mutsa.springboot_auction.domain.chat.repository.ChatRoomRepository;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.global.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final AuctionRepository auctionRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;


    /**
     * 채팅방 생성 또는 기존 채팅방 반환
     * 낙찰된 경매에 대해서만 생성 가능함
     */
    @Transactional
    public ChatRoomResponseDto createOrGetChatRoom(Long auctionId, Long userId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("경매를 찾을 수 없습니다"));

        if (auction.getStatus() != AuctionStatus.SOLD) {
            throw new RuntimeException("경매가 낙찰된 상태가 아닙니다");
        }

        User seller = auction.getSeller();
        User winner = auction.getWinner();

        if(!seller.getId().equals(userId) && !winner.getId().equals(userId)) {
            throw new RuntimeException("채팅방 접근 권한이 없습니다");
        }

        ChatRoom chatRoom = chatRoomRepository.findByAuctionAuctionId(auctionId)
                .orElseGet(() -> {
                    ChatRoom newRoom = new ChatRoom(seller, winner, auction);
                    return chatRoomRepository.save(newRoom);
                });

        return new ChatRoomResponseDto(chatRoom);
    }

    /**
     * 채팅방 모든 메시지 조회
     */
    @Transactional(readOnly = true)
    public List<MessageResponseDto> getMessages(Long roomId, Long userId) {
        // 채팅방 찾기
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() ->new RuntimeException("채팅방을 찾을 수 없습니다"));

        // 접근 권한(요청한 userId가 낙찰자 또는 판매자 맞는지 확인)
        if (!chatRoom.getBuyer().getId().equals(userId) && !chatRoom.getSeller().getId().equals(userId)) {
            new RuntimeException("접근 권한이 없습니다");
        }

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomClassroomIdOrderByCreatedAtAsc(chatRoom.getClassroomId());

        return messages.stream()
                .map(message -> new MessageResponseDto(
                        message.getMessageId(),
                        message.getUser().getId(),
                        message.getUser().getNickname(),
                        message.getMessageContent(),
                        message.getCreatedAt(),
                        message.getUser().getId().equals(userId)
                ))
                .collect(Collectors.toList());
    }


    /**
     * 메시지 저장
     * 방번호, 유저번호, 내용 받아서 ChatMessage객체 생성해서 db에 저장
     * 채팅메세지dto를 반환
     *
     */
    @Transactional
    public ChatMessageDto saveMessage(Long roomId, Long userId, String content) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다"));

        ChatMessage message =ChatMessage.builder()
                .user(user)
                .chatRoom(chatRoom)
                .messageContent(content)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        return new ChatMessageDto(savedMessage);
    }
}
