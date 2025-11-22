package com.mutsa.springboot_auction.domain.auction.controller;

import com.mutsa.springboot_auction.domain.auction.dto.AuctionDetailResponse;
import com.mutsa.springboot_auction.domain.auction.dto.AuctionSimpleResponse;
import com.mutsa.springboot_auction.domain.auction.dto.AuctionRequest;
import com.mutsa.springboot_auction.domain.auction.dto.DeckResponse;
import com.mutsa.springboot_auction.domain.auction.service.AuctionService;
import com.mutsa.springboot_auction.domain.auction.service.DeckService;
import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuctionController {

    private final AuctionService auctionService;
    private final DeckService deckService;
    private final UserRepository userRepository;

    // 경매 생성
    @PostMapping("/auctions")
    public ResponseEntity<Long> postAuction(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                                            @Valid @RequestBody
                                            AuctionRequest auctionRequest) {
        User user = customOAuth2User.getUser();
        return ResponseEntity.ok(auctionService.post(user, auctionRequest));
    }

    @GetMapping("/auctions/deck")
    public ResponseEntity<DeckResponse> getDeck(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = customOAuth2User.getUser().getId();
        List<AuctionSimpleResponse> items = deckService.getDeck(userId, size);
        return ResponseEntity.ok(DeckResponse.of(items));
    }

    // 경매상세조회
    @GetMapping("/auctions/{auctionId}")
    public ResponseEntity<AuctionDetailResponse> getAuction(@PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionService.get(auctionId));
    }



    @PostMapping("/auctions/test")
    public ResponseEntity<Long> postAuctionTest(@RequestParam Long userId,
                                            @Valid @RequestBody
                                            AuctionRequest auctionRequest) {
        log.info("되는건가 {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다"));
        log.info("되는건가 {}", user);
        return ResponseEntity.ok(auctionService.post(user, auctionRequest));
    }
}