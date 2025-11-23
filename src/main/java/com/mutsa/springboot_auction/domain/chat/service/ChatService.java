package com.mutsa.springboot_auction.domain.chat.service;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.entity.AuctionStatus;
import com.mutsa.springboot_auction.domain.auction.entity.PaymentStatus;
import com.mutsa.springboot_auction.domain.auction.repository.AuctionRepository;
import com.mutsa.springboot_auction.domain.bid.entity.Bid;
import com.mutsa.springboot_auction.domain.bid.entity.BidStatus;
import com.mutsa.springboot_auction.domain.bid.repositoy.BidRepository;
import com.mutsa.springboot_auction.domain.chat.dto.ChatMessageDto;
import com.mutsa.springboot_auction.domain.chat.dto.ChatRoomResponseDto;
import com.mutsa.springboot_auction.domain.chat.dto.MessageResponseDto;
import com.mutsa.springboot_auction.domain.chat.entity.ChatMessage;
import com.mutsa.springboot_auction.domain.chat.entity.ChatRoom;
import com.mutsa.springboot_auction.domain.chat.repository.ChatMessageRepository;
import com.mutsa.springboot_auction.domain.chat.repository.ChatRoomRepository;
import com.mutsa.springboot_auction.domain.pointHistory.entity.HistoryType;
import com.mutsa.springboot_auction.domain.pointHistory.entity.PointHistory;
import com.mutsa.springboot_auction.domain.pointHistory.repository.PointHistoryRepository;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final AuctionRepository auctionRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final BidRepository bidRepository;


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
                    ChatRoom newRoom = ChatRoom.builder()
                            .seller(seller)
                            .buyer(winner)
                            .auction(auction)
                            .build();
                    return chatRoomRepository.save(newRoom);
                });

        return ChatRoomResponseDto.builder()
                .chatRoomId(chatRoom.getChatroomId())
                .auctionId(chatRoom.getAuction().getAuctionId())
                .buyerId(chatRoom.getBuyer().getId())
                .buyerNickname(chatRoom.getBuyer().getNickname())
                .sellerId(chatRoom.getSeller().getId())
                .sellerNickname(chatRoom.getSeller().getNickname())
                .build();
    }

    /**
     * 채팅방 모든 메시지 조회
     */
    @Transactional
    public List<MessageResponseDto> getMessages(Long roomId, Long userId) {

        // 채팅방 찾기
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() ->new RuntimeException("채팅방을 찾을 수 없습니다"));

        // 접근 권한(요청한 userId가 낙찰자 또는 판매자 맞는지 확인)

        if (!chatRoom.getBuyer().getId().equals(userId) && !chatRoom.getSeller().getId().equals(userId)) {
            throw new RuntimeException("접근 권한이 없습니다");
        }

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomChatroomIdOrderByCreatedAtAsc(chatRoom.getChatroomId());

        messages.stream()
                .filter(msg -> !msg.getUser().getId().equals(userId))  // 상대방이 보낸 것만
                .filter(msg -> !msg.getIsRead())                        // 안 읽은 것만
                .forEach(msg -> msg.setIsRead(true));

        return messages.stream()
                .map(message -> new MessageResponseDto(
                        message.getMessageId(),
                        message.getUser().getId(),
                        message.getUser().getNickname(),
                        message.getMessageContent(),
                        message.getCreatedAt(),
                        message.getUser().getId().equals(userId),
                        message.getIsRead()
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

        ChatMessage message = ChatMessage.builder()
                .user(user)
                .chatRoom(chatRoom)
                .messageContent(content)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        return new ChatMessageDto(savedMessage);
    }

    // roomId, userId를 받아서 낙찰자가 판매자에게 포인트 송금하는 메서드
    @Transactional
    public void completeTransaction(Long roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다"));

        Auction auction = chatRoom.getAuction();

        if (auction.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new RuntimeException("이미 거래가 완료되었습니다");
        }

        if (!chatRoom.getBuyer().getId().equals(userId)) {
            throw new RuntimeException("거래 완료 권한이 없습니다");
        }

        User seller = chatRoom.getSeller();
        User buyer = chatRoom.getBuyer();
        Integer finalPrice = auction.getCurrentPrice();

        Integer remainingPrice = (int) (finalPrice * 0.9);

        if (buyer.getPoint() < remainingPrice) {
            throw new RuntimeException("포인트가 부족합니다");
        }

        buyer.subtractPoint(remainingPrice);
        seller.addPoint(finalPrice);

        PointHistory buyerHistory = PointHistory.builder()
                .user(buyer)
                .type(HistoryType.PURCHASE)
                .changeAmount(-remainingPrice)
                .build();

        pointHistoryRepository.save(buyerHistory);

        PointHistory sellerHistory = PointHistory.builder()
                .user(seller)
                .type(HistoryType.SALE)
                .changeAmount(finalPrice)
                .build();

        pointHistoryRepository.save(sellerHistory);

        auction.setPaymentStatus(PaymentStatus.COMPLETED);

        List<Bid> allBids = bidRepository.findAllByAuctionOrderByCreatedAtDesc(auction);
        for (Bid bid : allBids) {
            if (bid.getUser().getId().equals(buyer.getId())) {
                bid.setStatus(BidStatus.SUCCESS);
            } else {
                bid.setStatus(BidStatus.FAILED);
            }
        }
    }
}
