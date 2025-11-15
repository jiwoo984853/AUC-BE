package com.mutsa.springboot_auction.domain.bid.service;


import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.entity.AuctionStatus;
import com.mutsa.springboot_auction.domain.auction.entity.PaymentStatus;
import com.mutsa.springboot_auction.domain.auction.repository.AuctionRepository;
import com.mutsa.springboot_auction.domain.bid.dto.BidCreateRequestDto;
import com.mutsa.springboot_auction.domain.bid.dto.BidResponseDto;
import com.mutsa.springboot_auction.domain.bid.entity.Bid;
import com.mutsa.springboot_auction.domain.bid.repositoy.BidRepository;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;

@Service
@RequiredArgsConstructor
public class BidService {
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    @Transactional
    public BidResponseDto createBid(BidCreateRequestDto requestDto) {
        Auction auction = auctionRepository.findById(requestDto.getAuctionId())
                .orElseThrow(()-> new EntityNotFoundException("경매를 찾을 수 없습니다. id=" + requestDto.getAuctionId()));
        User bidder = userRepository.findById(requestDto.getUserId())
                .orElseThrow(()-> new EntityNotFoundException("사용자를 찾을 수 없습니다. id=" + requestDto.getUserId()));

        validateAuctionCanbid(auction);

        validateBidPriceHigherThanCurrent(auction, requestDto.getBidPrice());

        Bid prevTopBid = bidRepository.findFirstByAuctionOrderByBidPriceDesc(auction)
                .orElse(null);

        int deposit = (int) Math.ceil(requestDto.getBidPrice() * 0.1);

        if (bidder.getPoint() < requestDto.getBidPrice()) {
            throw new IllegalArgumentException("입찰에 필요한 포인트가 부족합니다. (입찰금: "
                    + requestDto.getBidPrice() + ", 보유 포인트 : " + bidder.getPoint() + ")");
        }

        if (prevTopBid != null) {
            User prevUser = prevTopBid.getUser();
            Integer prevDeposit = prevTopBid.getDepositAmount();
        }



    }

    private void validateAuctionCanbid(Auction auction) {
        if (auction.getStatus() != AuctionStatus.IN_PROGRESS)  {
            throw new IllegalArgumentException("현재 입찰이 불가능한 상태의 경매입니다.(status=" + auction.getStatus() + ")");

        }
        if (auction.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalArgumentException("이미 결제가 완료된 경매입니다.");
        }

        LocalDate now = LocalDate.now();
        if (auction.getStartAt() != null && now.isBefore(ChronoLocalDate.from(auction.getStartAt()))) {
            throw new IllegalArgumentException("아직 시작되지 않은 경매입니다.");
        }

        if (auction.getEndAt() != null && now.isAfter(ChronoLocalDate.from(auction.getEndAt()))) {
            throw new IllegalArgumentException("이미 종료된 경매입니다.");
        }
    }

    private void validateBidPriceHigherThanCurrent(Auction auction, Integer bidPrice) {
        Integer base = auction.getCurrentPrice() != null ? auction.getCurrentPrice() : auction.getStartPrice();
        if (base == null) base = 0;
        if (bidPrice == null || bidPrice <= base) {
            throw new IllegalArgumentException("입찰가는 현재가(" + base + ")보다 커야 합니다.");
        }
    }

    private BidResponseDto mapToResponseDto(Bid bid) {
        return new BidResponseDto(
                bid.getBidId(),
                bid.getUser().getId(),
                bid.getAuction().getAuctionId(),
                bid.getBidPrice(),
                bid.getDepositAmount(),
                bid.getStatus(),
                bid.getDepositStatus()
        );
    }
}
