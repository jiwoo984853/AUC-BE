package com.mutsa.springboot_auction.domain.auction.service;

import com.mutsa.springboot_auction.domain.auction.entity.ItemState;
import com.mutsa.springboot_auction.domain.auction.entity.UserAuctionState;
import com.mutsa.springboot_auction.domain.auction.repository.UserAuctionStateRepository;
import com.mutsa.springboot_auction.domain.auction.util.RedisKey;
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
    public void handleAction(Long userId, Long auctionId, String action) {
        switch(action.toUpperCase()) {
            case "DISLIKE" -> handleDisLike(userId, auctionId);
            case "HOLD" -> handleHold(userId, auctionId);
            default -> throw new IllegalArgumentException("Invalid action:" + action);
        }
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

        long showAgainTime = LocalDateTime.now()
                .plusMinutes(30)  // 30분 후 다시 등장 가능
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        // 1) HOLD ZSET에 넣기 (나중에 다시 덱에 넣기 위한 후보)
        redisTemplate.opsForZSet().add(holdKey, idStr, showAgainTime);

        // 2) 현재 덱에서 제거 (당장은 안 보이게)
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
