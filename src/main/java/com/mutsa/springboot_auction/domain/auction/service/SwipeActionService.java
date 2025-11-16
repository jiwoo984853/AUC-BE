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

        redisTemplate.opsForSet().add(dislikeKey, String.valueOf(auctionId));

        UserAuctionState state = getOrCreate(userId, auctionId);
        state.setState(ItemState.DISLIKED);
        state.setLastActionAt(LocalDateTime.now());
        userAuctionStateRepository.save(state);
    }

    private void handleHold(Long userId, Long auctionId) {
        String holdKey = RedisKey.holdKey(userId);

        long showAgainTime = LocalDateTime.now()
                .plusMinutes(30)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        redisTemplate.opsForZSet()
                .add(holdKey, String.valueOf(auctionId), showAgainTime);

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
