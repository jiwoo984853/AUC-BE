package com.mutsa.springboot_auction.domain.auction.service;

import com.mutsa.springboot_auction.domain.auction.entity.ItemState;
import com.mutsa.springboot_auction.domain.auction.entity.UserAuctionState;
import com.mutsa.springboot_auction.domain.auction.repository.UserAuctionStateRepository;
import com.mutsa.springboot_auction.domain.auction.util.RedisKey;
import com.mutsa.springboot_auction.domain.bid.repositoy.BidRepository;
import com.mutsa.springboot_auction.domain.bid.entity.Bid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SwipeActionService {
    private final RedisTemplate<String, String> redisTemplate;
    private final UserAuctionStateRepository userAuctionStateRepository;
    private final BidRepository bidRepository;
    public void handleAction(Long userId, Long auctionId, String action, Long bidId) {
        switch(action.toUpperCase()) {
            case "DISLIKE" -> handleDisLike(userId, auctionId);
            case "HOLD" -> handleHold(userId, auctionId);
            case "BIDDING" -> handleBidding(userId, auctionId, bidId);
            default -> throw new IllegalArgumentException("Invalid action:" + action);
        }
    }

    private void handleBidding(Long userId, Long auctionId, Long bidId) {
        Bid bid = bidId == null ? null : bidRepository.findById(bidId).orElse(null);
        if (bid == null || !bid.getAuction().getAuctionId().equals(auctionId)
                || !bid.getUser().getId().equals(userId)) {
            throw new IllegalStateException("입찰이 완료된 경매만 입찰 상태로 변경할 수 있습니다");
        }

        String dislikeKey = RedisKey.dislikeKey(userId); //재노출 방지
        String deckKey = RedisKey.deckKey(userId);

        String idStr = String.valueOf(auctionId);


        redisTemplate.opsForSet().add(dislikeKey, String.valueOf(auctionId));

        redisTemplate.opsForList().remove(deckKey, 0, idStr);


        UserAuctionState state = getOrCreate(userId, auctionId);
        state.setState(ItemState.BIDDING);
        state.setLastActionAt(LocalDateTime.now());
        userAuctionStateRepository.save(state);
    }

    private void handleDisLike(Long userId, Long auctionId) {
        String dislikeKey = RedisKey.dislikeKey(userId);
        String deckKey = RedisKey.deckKey(userId);

        String idStr = String.valueOf(auctionId);


        redisTemplate.opsForSet().add(dislikeKey, String.valueOf(auctionId));

        redisTemplate.opsForList().remove(deckKey, 0, idStr);


        UserAuctionState state = getOrCreate(userId, auctionId);
        state.setState(ItemState.DISLIKED);
        state.setLastActionAt(LocalDateTime.now());
        userAuctionStateRepository.save(state);
    }


    private void handleHold(Long userId, Long auctionId) {
        String holdKey = RedisKey.holdKey(userId);
        String deckKey = RedisKey.deckKey(userId);

        String idStr = String.valueOf(auctionId);

        // 1) HOLD 후보 풀(Set)에 넣기
        redisTemplate.opsForSet().add(holdKey, idStr);

        // 2) 현재 덱에서 제거
        redisTemplate.opsForList().remove(deckKey, 0, idStr);

        // 3) DB 상태 업데이트
        UserAuctionState state = getOrCreate(userId, auctionId);
        state.setState(ItemState.HOLD);
        state.setLastActionAt(LocalDateTime.now());
        state.setHoldCount(state.getHoldCount() + 1);
        userAuctionStateRepository.save(state);
    }

    private UserAuctionState getOrCreate(Long userId, Long auctionId) {
        return userAuctionStateRepository.findByUserIdAndAuctionId(userId, auctionId)
                .orElseGet(() -> UserAuctionState.create(userId, auctionId, ItemState.NEW));
    }
}
