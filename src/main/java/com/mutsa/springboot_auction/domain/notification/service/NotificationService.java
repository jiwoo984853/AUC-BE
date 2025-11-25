package com.mutsa.springboot_auction.domain.notification.service;

import com.mutsa.springboot_auction.domain.notification.dto.NotificationResponse;
import com.mutsa.springboot_auction.domain.notification.entity.Notification;
import com.mutsa.springboot_auction.domain.notification.entity.NotificationType;
import com.mutsa.springboot_auction.domain.notification.repository.NotificationRepository;
import com.mutsa.springboot_auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId){
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);

        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));

        return emitter;
    }

    public void sendOutbidNotification(Long userId, String goodsName, Long auctionId, Integer newBidAmount) {

        User user = User.builder()
                .id(userId)
                .build();

        Notification notification = Notification.builder()
                .user(user)
                .message(goodsName + "에서 " + newBidAmount + "원으로 입찰되었습니다")
                .auctionId(auctionId)
                .type(NotificationType.OUTBID)
                .build();

        notificationRepository.save(notification);

        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("outbid")
                        .data(Map.of(
                                "message", notification.getMessage(),
                                "auctionId", auctionId,
                                "newBidAmount", newBidAmount
                        )));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }

    }

    public void sendWinNotification(Long userId, String goodsName, Long auctionId, Integer finalPrice) {

        User user = User.builder()
                .id(userId)
                .build();

        Notification notification = Notification.builder()
                .user(user)
                .message(goodsName + "에서 " + finalPrice + "원으로 낙찰되었습니다")
                .auctionId(auctionId)
                .type(NotificationType.WIN)
                .build();

        notificationRepository.save(notification);

        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("win")
                        .data(Map.of(
                                "message", notification.getMessage(),
                                "auctionId", auctionId,
                                "finalPrice", finalPrice
                        )));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }

    public void sendNextWinnerOfferNotification(Long userId, String goodsName, Long auctionId, Integer offerPrice) {

        User user = User.builder()
                .id(userId)
                .build();

        Notification notification = Notification.builder()
                .user(user)
                .message(goodsName + " 경매에서 기존 낙찰자가 취소했습니다. " + offerPrice + "원에 구매하시겠습니까?")
                .auctionId(auctionId)
                .type(NotificationType.NEXT_WINNER_OFFER)
                .build();

        notificationRepository.save(notification);

        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("next-winner-offer")
                        .data(Map.of(
                                "message", notification.getMessage(),
                                "auctionId", auctionId,
                                "offerPrice", offerPrice
                        )));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }

    public void sendFailNotification(Long userId, String goodsName, Long auctionId) {

        User user = User.builder()
                .id(userId)
                .build();

        Notification notification = Notification.builder()
                .user(user)
                .message(goodsName + "의 경매가 입찰자 없이 종료되었습니다")
                .auctionId(auctionId)
                .type(NotificationType.FAIL) // ⚠️ enum에 FAIL 없으면 추가 필요
                .build();

        notificationRepository.save(notification);

        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("fail") // 프론트가 받을 이벤트 이름
                        .data(Map.of(
                                "message", notification.getMessage(),
                                "auctionId", auctionId
                        )));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }

    @Transactional
    public List<NotificationResponse> getNotificationList(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // 객체의 상태를 수정할때는 forEach를 쓰고, 객체를 변환(엔티티 -> dto)할떄는 map 사용
        notifications.stream()
                .filter(n -> !n.isRead())
                .forEach(n -> n.setRead(true));

        return notifications.stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public int getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalse(userId);

        notifications.forEach(notification -> notification.setRead(true));
    }
}
