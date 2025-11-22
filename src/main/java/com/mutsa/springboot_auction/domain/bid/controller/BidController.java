package com.mutsa.springboot_auction.domain.bid.controller;

import com.mutsa.springboot_auction.domain.bid.dto.BidCreateRequestDto;
import com.mutsa.springboot_auction.domain.bid.dto.BidResponseDto;
import com.mutsa.springboot_auction.domain.bid.dto.MyAuctionSummaryResponseDto;
import com.mutsa.springboot_auction.domain.bid.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BidController {

    private final BidService bidService;

    @PostMapping("/bids")
    public ResponseEntity<BidResponseDto> createBid(@Valid @RequestBody BidCreateRequestDto requestDto) {
        BidResponseDto responseDto = bidService.createBid(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/auctions/{auctionId}/bids")
    public ResponseEntity<List<BidResponseDto>> getBidsByAuction(@PathVariable Long auctionId) {
        List<BidResponseDto> bids = bidService.getBidsByAuctionId(auctionId);
        return ResponseEntity.ok(bids);
    }
    @GetMapping("/users/{userId}/auction-summary")
    public ResponseEntity<MyAuctionSummaryResponseDto> getMyAuctionSummary(@PathVariable Long userId) {
        MyAuctionSummaryResponseDto summary = bidService.getMyAuctionSummary(userId);
        return ResponseEntity.ok(summary);
    }
}
