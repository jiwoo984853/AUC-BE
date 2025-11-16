package com.mutsa.springboot_auction.domain.auction.service;


import com.mutsa.springboot_auction.domain.auction.dto.AuctionDto;
import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.repository.AuctionRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeckService {
    private final RedisTemplate<String, String> redisTemplate;
    private final AuctionRepository auctionRepository;

    private static final int DEFAULT_DECK_REFILL_SIZE = 30;

    public List<AuctionSimpleResponse> getDeck(Long userId, int size) {
        String deckKey = RedisKey.deckKey(userId);

        Long currentSize = redisTemplate.opsForList().size(deckKey);

        //현재 덱이 필요한 수보다 작다면 리필
        if (currentSize == null || currentSize < size) {
            int need = getNeed(size, currentSize);
            refillDeckFromDb(userId, Math.max(need, DEFAULT_DECK_REFILL_SIZE));
        }

        //리필한 레디스의 덱에서 size개 만큼 id를 가져옴(프론트에 전달할 id값)
        List<String> auctionIdStrings = redisTemplate.opsForList().range(deckKey, 0, size - 1);
        if (auctionIdStrings == null || auctionIdStrings.isEmpty()) {
            return List.of();
        }

        List<Long> auctionIds = auctionIdStrings.stream()
                .map(Long::valueOf).toList();

        //id로 레포지토리에서 조회
        List<Auction> auctions = auctionRepository.findAllById(auctionIds);
        Map<Long, Auction> auctionMap = auctions.stream()
                .collect(Collectors.toMap(Auction::getAuctionId, at -> at));

        List<AuctionSimpleResponse> result = new ArrayList<>();
        for (Long id : auctionIds) {
            Auction auction = auctionMap.get(id);
            if (auction != null) {
                result.add(AuctionSimpleResponse.from(auction));
            }
        }
        return result;
    }

    private static int getNeed(int size, Long currentSize) {
        int need = size - (currentSize == null ? 0 : currentSize.intValue());
        return need;
    }

    public void refillDeckFromDb(Long userId, int size) {
        String deckKey = RedisKey.deckKey(userId);
        String dislikeKey = RedisKey.dislikeKey(userId);

        List<String> currentDeck = redisTemplate.opsForList().range(deckKey, 0, -1);
        Set<String> currentDeckSet = getCurrentDeckSet(currentDeck);

        Set<String> disliked = getDisLikeIdSet(dislikeKey);

        LocalDateTime now = LocalDateTime.now();
        List<Auction> candidates = auctionRepository.findByEndAtAfterOrderByIdDesc(now);

        List<String> toPush = new ArrayList<>();

        //disLike, 현재 덱에 있으면 안넣음
        for (Auction auction : candidates) {
            String idStr = String.valueOf(auction.getAuctionId());
            if (disliked.contains(idStr)) {
                continue;
            }
            if (currentDeckSet.contains(idStr)) {
                continue;
            }
            toPush.add(idStr);
            if (toPush.size() >= size) {
                break;
            }
        }

        if (!toPush.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(deckKey, toPush);
        }
    }

    private Set<String> getDisLikeIdSet(String dislikeKey) {
        Set<String> disliked = redisTemplate.opsForSet().members(dislikeKey);
        if (disliked == null) {
            disliked = Set.of();
        }
        return disliked;
    }

    private static Set<String> getCurrentDeckSet(List<String> currentDeck) {
        return currentDeck == null ? null : new HashSet<>(currentDeck);
    }

    private String deckKey(Long userId) {
        return "DECK:" + userId;
    }

    private String dislikeKey(Long userId) {
        return "DisLike:" + userId;
    }
}
