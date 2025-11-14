package com.mutsa.springboot_auction.domain.search.controller;

import com.mutsa.springboot_auction.domain.search.dto.RecentSearchResponse;
import com.mutsa.springboot_auction.domain.search.dto.SearchFilterRequest;
import com.mutsa.springboot_auction.domain.search.dto.SearchResponse;
import com.mutsa.springboot_auction.domain.search.service.SearchService;
import jakarta.servlet.http.HttpSession;
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
            @RequestParam String keyword,
            @RequestParam Long userId) {
        List<SearchResponse> auctions = searchService.searchAuctionsInCategory(userId, categoryId, keyword);
        return ResponseEntity.ok(auctions);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SearchResponse>> searchAllAuctions(
            @RequestParam String keyword,
            @RequestParam Long userId,
            HttpSession session
    ) {
        List<SearchResponse> auctions = searchService.searchAllAuctions(userId, keyword);

        session.setAttribute("searchResults_" + userId, auctions);
        session.setAttribute("searchKeyword_" + userId, keyword);

        return ResponseEntity.ok(auctions);
    }

    @PostMapping("/all/filter")
    public ResponseEntity<List<SearchResponse>> applyFilter(
            @RequestParam Long userId,
            @RequestBody SearchFilterRequest filterRequest,
            HttpSession session) {

        @SuppressWarnings("unchecked")
        List<SearchResponse> searchResults =
                (List<SearchResponse>) session.getAttribute("searchResults_" + userId);

        if (searchResults == null) {
            throw new RuntimeException("검색 결과가 없습니다. 먼저 검색을 해주세요");
        }

        List<SearchResponse> filteredResults =
                searchService.applyFiltersToSessionResults(searchResults, filterRequest);

        return ResponseEntity.ok(filteredResults);
    }


    @GetMapping("/recent")
    public ResponseEntity<List<RecentSearchResponse>> getRecentSearches(
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(searchService.getRecentSearches(userId));
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
