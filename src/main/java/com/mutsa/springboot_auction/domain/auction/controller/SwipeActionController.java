package com.mutsa.springboot_auction.domain.auction.controller;

import com.mutsa.springboot_auction.domain.auction.dto.SwipeActionRequest;
import com.mutsa.springboot_auction.domain.auction.service.SwipeActionService;
import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/swipes")
public class SwipeActionController {
    private final SwipeActionService swipeActionService;

    @PostMapping("/action")
    public ResponseEntity<?> swipeAction(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                                        @RequestBody SwipeActionRequest request) {

        swipeActionService.handleAction(customOAuth2User.getUser().getId(),
                request.getAuctionId(),
                request.getAction());
        return ResponseEntity.ok().body(Map.of("success", true));
    }
}
