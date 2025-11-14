package com.mutsa.springboot_auction.domain.search.service;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.entity.AuctionStatus;
import com.mutsa.springboot_auction.domain.search.dto.RecentSearchResponse;
import com.mutsa.springboot_auction.domain.search.dto.SearchFilterRequest;
import com.mutsa.springboot_auction.domain.search.dto.SearchResponse;
import com.mutsa.springboot_auction.domain.search.entity.RecentSearch;
import com.mutsa.springboot_auction.domain.search.repository.RecentSearchRepository;
import com.mutsa.springboot_auction.domain.search.repository.SearchRepository;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchRepository searchRepository;
    private final UserRepository userRepository;
    private final RecentSearchRepository recentSearchRepository;

    // 카테고리 검색하면 그에 맞는 경매 물품 반환
    @Transactional(readOnly = true)
    public List<SearchResponse> getAuctionsByCategory(Long categoryId) {
        List<Auction> auctions = searchRepository.getAuctionsByCategory(categoryId);

        return auctions.stream()
                .map(auction -> this.convertToResponse(auction))
                .collect(Collectors.toList());
    }

    // 카테고리 내에서 검색하면 카테고리 내 키워드에 맞는 경매물품 반환
    @Transactional
    public List<SearchResponse> searchAuctionsInCategory(Long userId, Long categoryId, String keyword) {
        String processedKeyword = keyword.replaceAll("\\s+", "");
        List<Auction> auctions = searchRepository.searchInCategory(categoryId, processedKeyword);

        saveRecentSearch(userId, keyword);

        return auctions.stream()
                .map(auction -> this.convertToResponse(auction))
                .collect(Collectors.toList());
    }

    // 전체 카테고리에서 검색
    @Transactional
    public List<SearchResponse> searchAllAuctions(Long userId, String keyword) {
        String processedKeyword = keyword.replaceAll("\\s+", "");
        List<Auction> auctions = searchRepository.searchAllAuctions(processedKeyword);

        saveRecentSearch(userId, keyword);

        return auctions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 최근 검색어 저장(만약 이미 있다면 삭제 후 새로 등록 - 최근시간으로 정렬하기 위해)
    @Transactional
    public void saveRecentSearch(Long userId, String keyword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다"));

        RecentSearch existing = recentSearchRepository.findByUserAndKeyword(user, keyword);
        if (existing != null) {
            recentSearchRepository.delete(existing);
        }

        RecentSearch newSearch = RecentSearch.builder()
                .user(user)
                .keyword(keyword)
                .build();

        recentSearchRepository.save(newSearch);
    }

    @Transactional
    public void deleteRecentSearch(Long searchId, Long userId) {
        RecentSearch recentSearch = recentSearchRepository.findById(searchId)
                .orElseThrow(() -> new RuntimeException("최근 검색어를 찾을 수 없습니다"));

        if (!recentSearch.getUser().getId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다");
        }

        recentSearchRepository.deleteById(searchId);
    }

    @Transactional(readOnly = true)
    public List<RecentSearchResponse> getRecentSearches(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다"));

        List<RecentSearch> recentSearches = recentSearchRepository.findTop10ByUserOrderByCreatedAtDesc(user);

        return recentSearches.stream()
                .map(rs -> RecentSearchResponse.builder()
                        .id(rs.getId())
                        .keyword(rs.getKeyword())
                        .build())
                .collect(Collectors.toList());
    }

    public List<SearchResponse> applyFiltersToSessionResults(
            List<SearchResponse> searchResults,
            SearchFilterRequest filterRequest
    ) {
        return searchResults.stream()
                .filter(auction -> {
                    // null이 아니고 empty가 아니면
                    if (filterRequest.getConditions() != null && !filterRequest.getConditions().isEmpty()) {
                        // 요청필터에 들어가있는 것이 경매물품에 없으면 false 반환해서 필터링
                        if (!filterRequest.getConditions().contains(auction.getCondition())) {
                            return false;
                        }
                    }

                    if (filterRequest.getMinYear() != null || filterRequest.getMaxYear() != null) {
                        // 경매 물품에 연식이 없으면 필터링
                        if (auction.getYear() == null) {
                            return false;
                        }

                        if (filterRequest.getMinYear() != null) {
                            // 경매 물품 연식이 최소연식보다 작으면 필터링
                            if (auction.getYear().compareTo(filterRequest.getMinYear()) < 0) {
                                return false;
                            }
                        }

                        if (filterRequest.getMaxYear() != null) {
                            if (auction.getYear().compareTo(filterRequest.getMaxYear()) > 0) {
                                return false;
                            }
                        }
                    }

                    if (filterRequest.getMinPrice() != null) {
                        if (auction.getCurrentPrice() < filterRequest.getMinPrice()) {
                            return false;
                        }
                    }

                    if (filterRequest.getMaxPrice() != null) {
                        if (auction.getCurrentPrice() > filterRequest.getMaxPrice()) {
                            return false;
                        }
                    }

                    if (filterRequest.getCategoryIds() != null && !filterRequest.getCategoryIds().isEmpty()) {
                        if (!filterRequest.getCategoryIds().contains(auction.getCategoryId())) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }


    // 경매 엔티티를 dto로 변환
    private SearchResponse convertToResponse(Auction auction) {
        String imageUrl = (auction.getImages() != null && !auction.getImages().isEmpty())
                ? auction.getImages().get(0).getImageUrl()
                : null;

        Long categoryId = auction.getCategories().isEmpty()
                ? null
                : auction.getCategories().get(0).getCategory().getCategoryId();

        String statusText = getStatusText(auction.getStatus());
        String remainingTime = calculateRemainingTime(auction.getEndAt());

        return SearchResponse.builder()
                .auctionId(auction.getAuctionId())
                .goodsName(auction.getGoodsName())
                .currentPrice(auction.getCurrentPrice())
                .remainingTime(remainingTime)
                .status(statusText)
                .imageUrl(imageUrl)
                .year(auction.getYear())
                .categoryId(categoryId)
                .condition(auction.getCondition())
                .build();
    }

    private String getStatusText(AuctionStatus status) {
        return switch (status) {
            case IN_PROGRESS -> "경매중";
            case SOLD -> "경매종료";
            default -> status.name();
        };
    }

    private String calculateRemainingTime(LocalDateTime endAt) {
        if (endAt == null) return "시간 정보 없음";

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endAt)) {
            return "경매 종료";
        }

        Duration duration =  Duration.between(now, endAt);

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        if (days > 0) {
            return days + "일 남음";
        } else if (hours > 0) {
            return hours + "시간 남음";
        } else {
            return minutes + "분 남음";
        }
    }
}
