package com.mutsa.springboot_auction.domain.search.service;

import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.entity.AuctionStatus;
import com.mutsa.springboot_auction.domain.search.dto.SearchResponse;
import com.mutsa.springboot_auction.domain.search.repository.SearchRepository;
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

    @Transactional(readOnly = true)
    public List<SearchResponse> getAuctionsByCategory(Long categoryId) {
        List<Auction> auctions = searchRepository.getAuctionsByCategory(categoryId);

        return auctions.stream()
                .map(auction -> this.convertToResponse(auction))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SearchResponse> searchAuctionsInCategory(Long categoryId, String keyword) {
        List<Auction> auctions = searchRepository.searchInCategory(categoryId, keyword);

        return auctions.stream()
                .map(auction -> this.convertToResponse(auction))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SearchResponse> searchAllAuctions(String keyword) {
        List<Auction> auctions = searchRepository.searchAllAuctions(keyword);
        return auctions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private SearchResponse convertToResponse(Auction auction) {
        String imageUrl = (auction.getImages() != null && !auction.getImages().isEmpty())
                ? auction.getImages().get(0).getImageUrl()
                : null;

        String statusText = getStatusText(auction.getStatus());
        String remainingTime = calculateRemainingTime(auction.getEndAt());

        return SearchResponse.builder()
                .auctionId(auction.getAuctionId())
                .goodsName(auction.getGoodsName())
                .currentPrice(auction.getCurrentPrice())
                .remainingTime(remainingTime)
                .status(statusText)
                .imageUrl(imageUrl)
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
