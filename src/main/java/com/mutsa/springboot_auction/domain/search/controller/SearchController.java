package com.mutsa.springboot_auction.domain.search.controller;

import com.mutsa.springboot_auction.domain.search.dto.RecentSearchResponse;
import com.mutsa.springboot_auction.domain.search.dto.SearchResponse;
import com.mutsa.springboot_auction.domain.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
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
            @RequestParam String keyword,
            @RequestParam Long userId) {
        List<SearchResponse> auctions = searchService.searchAuctionsInCategory(userId, categoryId, keyword);
        return ResponseEntity.ok(auctions);
    }

    @GetMapping("/keyword")
    public ResponseEntity<List<SearchResponse>> getAllAuctions(
            @RequestParam String keyword,
            @RequestParam Long userId
    ) {
        List<SearchResponse> auctions = searchService.searchAllAuctions(userId, keyword);
        return ResponseEntity.ok(auctions);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RecentSearchResponse>> getRecentSearches(
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(searchService.getRecentSearch(userId));
    }

    @DeleteMapping("/recent/{recentId}")
    public ResponseEntity<Void> deleteRecentSearch(
            @PathVariable Long recentId,
            @RequestParam Long userId
    ) {
        searchService.deleteRecentSearch(recentId, userId);
        return ResponseEntity.ok().build();
    }
}
