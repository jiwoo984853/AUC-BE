package com.mutsa.springboot_auction.domain.auction.service;

import com.mutsa.springboot_auction.domain.auction.repository.UserAuctionStateRepository;
import com.mutsa.springboot_auction.domain.auction.util.RedisKey;
import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.entity.ItemState;
import com.mutsa.springboot_auction.domain.auction.entity.UserAuctionState;
import com.mutsa.springboot_auction.domain.bid.entity.Bid;
import com.mutsa.springboot_auction.domain.bid.repositoy.BidRepository;
import com.mutsa.springboot_auction.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SwipeActionServiceTest {
    @Test
    void failedBidCannotRemoveAuctionFromDeck() {
        RedisTemplate<String, String> redis = mock(RedisTemplate.class);
        UserAuctionStateRepository states = mock(UserAuctionStateRepository.class);
        BidRepository bids = mock(BidRepository.class);
        SwipeActionService service = new SwipeActionService(redis, states, bids);

        assertThrows(IllegalStateException.class,
                () -> service.handleAction(42L, 7L, "BIDDING", null));

        verifyNoInteractions(bids);
        verifyNoInteractions(redis, states);
    }

    @Test
    void anotherUsersBidCannotRemoveAuctionFromDeck() {
        RedisTemplate<String, String> redis = mock(RedisTemplate.class);
        UserAuctionStateRepository states = mock(UserAuctionStateRepository.class);
        BidRepository bids = mock(BidRepository.class);
        Bid bid = mock(Bid.class);
        Auction auction = mock(Auction.class);
        User bidder = mock(User.class);
        when(auction.getAuctionId()).thenReturn(7L);
        when(bidder.getId()).thenReturn(99L);
        when(bid.getAuction()).thenReturn(auction);
        when(bid.getUser()).thenReturn(bidder);
        when(bids.findById(10L)).thenReturn(Optional.of(bid));
        SwipeActionService service = new SwipeActionService(redis, states, bids);

        assertThrows(IllegalStateException.class,
                () -> service.handleAction(42L, 7L, "BIDDING", 10L));

        verifyNoInteractions(redis, states);
    }

    @Test
    @SuppressWarnings("unchecked")
    void successfulBidCanRemoveItsAuctionFromDeck() {
        RedisTemplate<String, String> redis = mock(RedisTemplate.class);
        SetOperations<String, String> sets = mock(SetOperations.class);
        ListOperations<String, String> lists = mock(ListOperations.class);
        when(redis.opsForSet()).thenReturn(sets);
        when(redis.opsForList()).thenReturn(lists);
        UserAuctionStateRepository states = mock(UserAuctionStateRepository.class);
        BidRepository bids = mock(BidRepository.class);
        Bid bid = mock(Bid.class);
        Auction auction = mock(Auction.class);
        User bidder = mock(User.class);
        when(auction.getAuctionId()).thenReturn(7L);
        when(bidder.getId()).thenReturn(42L);
        when(bid.getAuction()).thenReturn(auction);
        when(bid.getUser()).thenReturn(bidder);
        when(bids.findById(10L)).thenReturn(Optional.of(bid));
        UserAuctionState state = UserAuctionState.create(42L, 7L, ItemState.NEW);
        when(states.findByUserIdAndAuctionId(42L, 7L)).thenReturn(Optional.of(state));
        SwipeActionService service = new SwipeActionService(redis, states, bids);

        service.handleAction(42L, 7L, "BIDDING", 10L);

        assertEquals(ItemState.BIDDING, state.getState());
        verify(states).save(state);
        verify(lists).remove(RedisKey.deckKey(42L), 0, "7");
    }
}
