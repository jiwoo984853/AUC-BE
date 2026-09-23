package com.mutsa.springboot_auction.domain.auction.service;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.repository.AuctionRepository;
import com.mutsa.springboot_auction.domain.auction.util.RedisKey;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Set;
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
        when(auctions.findAllById(any())).thenAnswer(invocation -> {
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
}
