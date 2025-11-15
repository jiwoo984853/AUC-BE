package com.mutsa.springboot_auction.domain.auction.controller;

import com.mutsa.springboot_auction.domain.auction.dto.AuctionDto;
import com.mutsa.springboot_auction.domain.auction.dto.AuctionRequest;
import com.mutsa.springboot_auction.domain.auction.dto.DeckResponse;
import com.mutsa.springboot_auction.domain.auction.service.AuctionService;
import com.mutsa.springboot_auction.domain.auction.service.DeckService;
import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import com.mutsa.springboot_auction.domain.user.entity.User;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;
    private final DeckService deckService;

    @PostMapping("/auctions")
    public ResponseEntity<Long> postAuction(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                                            @Valid @RequestBody
                                            AuctionRequest auctionRequest) {
        User user = customOAuth2User.getUser();
        return ResponseEntity.ok(auctionService.post(user, auctionRequest));
    }

    @GetMapping("auctions/deck")
    public DeckResponse getDeck(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User, // 너 프로젝트의 유저 디테일 타입
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = customOAuth2User.getUser().getId();
        List<AuctionDto> items = deckService.getDeck(userId, size);
        return DeckResponse.of(items);
    }
}