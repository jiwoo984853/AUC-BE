package com.mutsa.springboot_auction.domain.auction.service;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.repository.AuctionRepository;
import com.mutsa.springboot_auction.domain.auction.util.RedisKey;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Set;
import java.time.LocalDateTime;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DeckServiceTest {
    @Test
    @SuppressWarnings("unchecked")
    void remainingCardsAreExcludedAndNextCardsAreReturned() {
        RedisTemplate<String, String> redis = mock(RedisTemplate.class);
        ListOperations<String, String> lists = mock(ListOperations.class);
        when(redis.opsForList()).thenReturn(lists);
        String deckKey = RedisKey.deckKey(42L);
        when(lists.size(deckKey)).thenReturn(13L);
        when(lists.range(deckKey, 0, 12)).thenReturn(
                LongStream.rangeClosed(1, 13).mapToObj(String::valueOf).toList());

        AuctionRepository auctions = mock(AuctionRepository.class);
        when(auctions.findEligibleDeckAuctions(any(), eq(42L), any(LocalDateTime.class))).thenAnswer(invocation -> {
            List<Long> ids = invocation.getArgument(0);
            return ids.stream().map(id -> {
                Auction auction = mock(Auction.class);
                when(auction.getAuctionId()).thenReturn(id);
                when(auction.getImages()).thenReturn(List.of());
                return auction;
            }).toList();
        });

        var result = new DeckService(redis, auctions).getDeck(42L, 10, Set.of(1L, 2L, 3L));

        assertEquals(LongStream.rangeClosed(4, 13).boxed().toList(),
                result.stream().map(item -> item.getId()).toList());
        verify(lists).range(deckKey, 0, 12);
    }

    @Test
    @SuppressWarnings("unchecked")
    void sellersOwnAuctionIsRemovedFromExistingDeck() {
        RedisTemplate<String, String> redis = mock(RedisTemplate.class);
        ListOperations<String, String> lists = mock(ListOperations.class);
        when(redis.opsForList()).thenReturn(lists);
        String deckKey = RedisKey.deckKey(42L);
        when(lists.range(deckKey, 0, -1)).thenReturn(List.of("7", "8"));
        when(lists.size(deckKey)).thenReturn(2L);
        when(lists.range(deckKey, 0, 1)).thenReturn(List.of("7", "8"));

        AuctionRepository auctions = mock(AuctionRepository.class);
        Auction otherSellerAuction = mock(Auction.class);
        when(otherSellerAuction.getAuctionId()).thenReturn(8L);
        when(otherSellerAuction.getImages()).thenReturn(List.of());
        when(auctions.findEligibleDeckAuctions(any(), eq(42L), any(LocalDateTime.class)))
                .thenReturn(List.of(otherSellerAuction));

        var result = new DeckService(redis, auctions).getDeck(42L, 2, Set.of());

        assertEquals(List.of(8L), result.stream().map(item -> item.getId()).toList());
        verify(lists).remove(deckKey, 0, "7");
        verify(lists, never()).remove(deckKey, 0, "8");
    }
}
