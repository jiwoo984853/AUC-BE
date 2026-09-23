package com.mutsa.springboot_auction.domain.bid.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.mutsa.springboot_auction.domain.bid.dto.BidCreateRequestDto;
import com.mutsa.springboot_auction.domain.bid.service.BidService;
import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import com.mutsa.springboot_auction.domain.user.entity.User;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BidControllerTest {

    @Test
    void createBidUsesAuthenticatedUserEvenWhenBodyContainsAnotherUserId() throws Exception {
        BidService bidService = mock(BidService.class);
        BidController controller = new BidController(bidService);
        User user = mock(User.class);
        when(user.getId()).thenReturn(42L);
        CustomOAuth2User currentUser = new CustomOAuth2User(user, Map.of());

        BidCreateRequestDto request = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(
                "{\"auctionId\":7,\"userId\":999,\"bidPrice\":10000}",
                BidCreateRequestDto.class
        );

        controller.createBid(currentUser, request);

        verify(bidService).createBid(42L, request);
    }
}
