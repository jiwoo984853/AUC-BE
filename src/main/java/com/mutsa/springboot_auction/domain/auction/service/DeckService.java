package com.mutsa.springboot_auction.domain.auction.service;


import com.mutsa.springboot_auction.domain.auction.dto.AuctionSimpleResponse;
import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.repository.AuctionRepository;
import com.mutsa.springboot_auction.domain.auction.util.RedisKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
            refillDeck(userId, Math.max(need, DEFAULT_DECK_REFILL_SIZE));
        }

        //리필한 레디스의 덱에서 size개 만큼 id를 가져옴(프론트에 전달할 id값)
        List<String> auctionIdStrings = redisTemplate.opsForList().range(deckKey, 0, size - 1);
        if (auctionIdStrings == null || auctionIdStrings.isEmpty()) {
            return List.of();
        }

        List<Long> auctionIds = auctionIdStrings.stream()
                .map(Long::valueOf).toList();

        //id로 레포지토리에서 조회
        return getAuctionSimpleResponses(auctionIds);
    }

    private List<AuctionSimpleResponse> getAuctionSimpleResponses(List<Long> auctionIds) {
        //레디스 순서 그대로 가져옴
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
        return size - (currentSize == null ? 0 : currentSize.intValue());
    }

    public void refillDeck(Long userId, int size) {
        String deckKey = RedisKey.deckKey(userId);
        String dislikeKey = RedisKey.dislikeKey(userId);
        String holdKey = RedisKey.holdKey(userId);

        List<String> currentDeck = redisTemplate.opsForList().range(deckKey, 0, -1);
        Set<String> currentDeckSet = getCurrentDeckSet(currentDeck);
        Set<String> disliked = getDisLikeIdSet(dislikeKey);

        int maxHoldToUse = 1;  // 한 번 리필할 때 hold에서 최대 1개만 쓰자 같은 느낌 (연속으로 나오는거 싫어서 일케 함)
        int pushedFromHold = refillFromHoldRandom(holdKey, deckKey, size, maxHoldToUse, disliked, currentDeckSet);

        int needFromDb = size - pushedFromHold;
        if (needFromDb <= 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<Auction> candidates = auctionRepository.findByEndAtAfterOrderByAuctionIdDesc(now);

        List<String> toPush = filterCandidates(size, candidates, disliked, currentDeckSet);

        if (!toPush.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(deckKey, toPush);
        }
    }

    private int refillFromHoldRandom(
            String holdKey,
            String deckKey,
            int size,
            int maxHoldToUse,
            Set<String> disliked,
            Set<String> currentDeckSet
    ) {
        Set<String> holdSet = redisTemplate.opsForSet().members(holdKey);
        if (holdSet == null || holdSet.isEmpty()) {
            return 0;
        }

        List<String> shuffled = new ArrayList<>(holdSet);
        Collections.shuffle(shuffled); // 랜덤 순서

        List<String> toPush = new ArrayList<>();
        for (String idStr : shuffled) {
            if (disliked.contains(idStr)) continue;
            if (currentDeckSet.contains(idStr)) continue;

            toPush.add(idStr);

            if (toPush.size() >= maxHoldToUse) break; // 한 번에 너무 많이 안 쓰기
            if (toPush.size() >= size) break;         // 필요한 사이즈 이상이면 중단
        }

        if (toPush.isEmpty()) {
            return 0;
        }

        // 덱에 다시 넣기 (앞/뒤 정책은 알아서)
        redisTemplate.opsForList().rightPushAll(deckKey, toPush);

        // hold 풀에서 빼기 (다시 보여줬으니까)
        redisTemplate.opsForSet().remove(holdKey, toPush.toArray());

        return toPush.size();
    }

    private List<String> filterCandidates(int size, List<Auction> candidates, Set<String> disliked,
                                          Set<String> currentDeckSet) {
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
        return toPush;
    }

    private Set<String> getDisLikeIdSet(String dislikeKey) {
        Set<String> disliked = redisTemplate.opsForSet().members(dislikeKey);
        if (disliked == null) {
            disliked = Set.of();
        }
        return disliked;
    }

    private Set<String> getCurrentDeckSet(List<String> currentDeck) {
        return currentDeck == null ? Set.of() : new HashSet<>(currentDeck);
    }
}
