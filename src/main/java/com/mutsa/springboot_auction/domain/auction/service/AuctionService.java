package com.mutsa.springboot_auction.domain.auction.service;

import com.mutsa.springboot_auction.domain.auction.dto.AuctionDetailResponse;
import com.mutsa.springboot_auction.domain.auction.dto.AuctionRequest;
import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.repository.AuctionRepository;
import com.mutsa.springboot_auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    public Long post(User seller, AuctionRequest auctionRequest) {
        //request -> entity
        Auction auction = Auction.of(auctionRequest, seller);
        //저장
        Auction savedAuction = auctionRepository.save(auction);
        return savedAuction.getAuctionId();
    }


    public AuctionDetailResponse get(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 Id의 경매가 존재하지 않습니다"));
        return AuctionDetailResponse.from(auction);
    }
}