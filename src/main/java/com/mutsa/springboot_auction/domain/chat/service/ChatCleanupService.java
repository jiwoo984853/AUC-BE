package com.mutsa.springboot_auction.domain.chat.service;

import com.mutsa.springboot_auction.domain.auction.entity.PaymentStatus;
import com.mutsa.springboot_auction.domain.chat.entity.ChatRoom;
import com.mutsa.springboot_auction.domain.chat.repository.ChatMessageRepository;
import com.mutsa.springboot_auction.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatCleanupService {
    private static final int CHAT_RETENTION_DAYS = 90;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void deleteExpiredChatRooms() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(CHAT_RETENTION_DAYS);
        List<ChatRoom> expiredRooms = chatRoomRepository
                .findAllByAuctionPaymentStatusAndAuctionPaymentCompletedAtBefore(
                        PaymentStatus.COMPLETED,
                        cutoff
                );

        if (expiredRooms.isEmpty()) {
            return;
        }

        List<Long> roomIds = expiredRooms.stream()
                .map(ChatRoom::getChatroomId)
                .toList();

        int deletedMessageCount = chatMessageRepository.deleteAllByChatRoomIds(roomIds);
        chatRoomRepository.deleteAllByIdInBatch(roomIds);

        log.info(
                "Deleted expired chat rooms: roomCount={}, messageCount={}, cutoff={}",
                roomIds.size(),
                deletedMessageCount,
                cutoff
        );
    }
}
