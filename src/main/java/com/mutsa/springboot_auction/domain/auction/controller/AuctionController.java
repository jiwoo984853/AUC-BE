package com.mutsa.springboot_auction.domain.auction.controller;

import com.mutsa.springboot_auction.domain.auction.dto.AuctionRequest;
import com.mutsa.springboot_auction.domain.auction.service.AuctionService;
import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import com.mutsa.springboot_auction.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @PostMapping
    public ResponseEntity<Long> postAuction(@AuthenticationPrincipal CustomOAuth2User customOAuth2User, @Valid @RequestBody
    AuctionRequest auctionRequest) {
        User user = customOAuth2User.getUser();
        return ResponseEntity.ok(auctionService.post(user, auctionRequest));
    }
}