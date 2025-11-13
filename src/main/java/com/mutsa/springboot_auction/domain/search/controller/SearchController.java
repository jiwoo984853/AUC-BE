package com.mutsa.springboot_auction.domain.search.controller;

import com.mutsa.springboot_auction.domain.search.dto.SearchResponse;
import com.mutsa.springboot_auction.domain.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<SearchResponse>> getAuctionsByCategory(@PathVariable Long categoryId) {
        List<SearchResponse> auctions = searchService.getAuctionsByCategory(categoryId);
        return ResponseEntity.ok(auctions);
    }

    @GetMapping("/category/{categoryId}/keyword")
    public ResponseEntity<List<SearchResponse>> searchAuctionsInCategory(
            @PathVariable Long categoryId,
            @RequestParam String keyword) {
        List<SearchResponse> auctions = searchService.searchAuctionsInCategory(categoryId, keyword);
        return ResponseEntity.ok(auctions);
    }
}
