package com.mutsa.springboot_auction.domain.auction.service;

import com.mutsa.springboot_auction.domain.auction.dto.AuctionDetailResponse;
import com.mutsa.springboot_auction.domain.auction.dto.AuctionListResponse;
import com.mutsa.springboot_auction.domain.auction.dto.AuctionRequest;
import com.mutsa.springboot_auction.domain.auction.dto.AuctionSimpleResponse;
import com.mutsa.springboot_auction.domain.auction.dto.RefuseToPurchaseResponse;
import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.entity.AuctionStatus;
import com.mutsa.springboot_auction.domain.auction.entity.PaymentStatus;
import com.mutsa.springboot_auction.domain.auction.repository.AuctionRepository;
import com.mutsa.springboot_auction.domain.auctionCategory.entity.AuctionCategory;
import com.mutsa.springboot_auction.domain.auctionImage.entity.AuctionImage;
import com.mutsa.springboot_auction.domain.auctionImage.repository.AuctionImageRepository;
import com.mutsa.springboot_auction.domain.bid.entity.Bid;
import com.mutsa.springboot_auction.domain.bid.entity.BidStatus;
import com.mutsa.springboot_auction.domain.bid.entity.DepositStatus;
import com.mutsa.springboot_auction.domain.bid.repositoy.BidRepository;
import com.mutsa.springboot_auction.domain.category.entity.Category;
import com.mutsa.springboot_auction.domain.category.repository.CategoryRepository;
import com.mutsa.springboot_auction.domain.notification.service.NotificationService;
import com.mutsa.springboot_auction.domain.pointHistory.service.PointService;
import com.mutsa.springboot_auction.domain.user.dto.UserResponse;
import com.mutsa.springboot_auction.domain.user.entity.User;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final AuctionImageRepository auctionImageRepository;
    private final CategoryRepository categoryRepository;
    private final BidRepository bidRepository;
    private final NotificationService notificationService;
    private final PointService pointService;

    @Transactional
    public Long post(User seller, AuctionRequest auctionRequest) {
        // 1. Auction 엔티티 생성 (아직 저장하지 않음)
        Auction auction = Auction.of(auctionRequest, seller);

        // 2. Category 조회
        List<Category> categories = categoryRepository.findAllById(auctionRequest.getCategoryIds()); // ID 타입 확인!

        for (Category category : categories) {
            // 3. AuctionCategory 객체 생성
            //    복합키 ID는 나중에 Hibernate가 채우므로, ID 필드는 건드리지 않습니다.
            AuctionCategory auctionCategory = AuctionCategory.builder()
                    // category 필드를 명시적으로 채워줍니다.
                    .category(category)
                    // .id(null) or .id(new AuctionCategoryId())는 피함
                    .build();

            // 4. Auction과 AuctionCategory의 양방향 관계 설정
            //    (addCategory 내부에서 auctionCategory.setAuction(auction) 호출)
            auction.addCategory(auctionCategory);
        }

        // 5. 🚨 Auction 저장 (모든 관계 설정 후 저장) 🚨
        // 이 시점에 auction_id가 할당되고, CascadeType.ALL에 의해 AuctionCategory도 함께 저장됩니다.
        Auction savedAuction = auctionRepository.save(auction);

        // 6. AuctionImage 처리 (savedAuction의 ID를 사용해 저장)
        List<AuctionImage> auctionImages = new ArrayList<>();
        for (String imageUrl : auctionRequest.getImageUrls()) {
            auctionImages.add(new AuctionImage(imageUrl, savedAuction));
        }
        auctionImageRepository.saveAll(auctionImages);

        return savedAuction.getAuctionId();
    }


    public AuctionDetailResponse get(Long auctionId, User viewer) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 Id의 경매가 존재하지 않습니다"));
        long totalNumOfBidder = bidRepository.countUniqueBidders(auctionId);
        Optional<Bid> userBid = bidRepository.findTopByAuction_AuctionIdAndUserIdOrderByCreatedAtDesc(
                auctionId, viewer.getId());
        Integer myBidPrice = userBid.map(Bid::getBidPrice).orElse(null);
        return AuctionDetailResponse.from(auction, totalNumOfBidder, myBidPrice);
    }

    public AuctionListResponse getMyAuctions(User viewer) {
        return AuctionListResponse.of(auctionRepository.findBySeller(viewer).stream()
                .map(AuctionSimpleResponse::from).toList());
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void closeExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Auction> auctions = auctionRepository.findAuctionsToClose(now);

        for (Auction auction : auctions) {
            if (auction.getWinner() != null) {
                auction.setStatus(AuctionStatus.CLOSED);
                notificationService.sendWinNotification(auction.getWinner().getId(),auction.getGoodsName(),auction.getAuctionId(), auction.getCurrentPrice());
            } else{
                auction.setStatus(AuctionStatus.CLOSED);
                notificationService.sendFailNotification(auction.getSeller().getId(), auction.getGoodsName(),
                        auction.getAuctionId());
            }
        }
    }

    @Transactional
    public RefuseToPurchaseResponse refuseToPurchase(Long auctionId, User purchaser) {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 Id의 경매가 존재하지 않습니다"));
        if (auction.getWinner().equals(purchaser)) {
            throw new IllegalArgumentException("최상위 입찰자가 아닙니다.");
        }
        //bid 엔티티 찾아!
        Bid bid = bidRepository.findTopByAuction_AuctionIdAndUserIdOrderByCreatedAtDesc(auctionId, purchaser.getId())
                .orElseThrow(() -> new IllegalStateException("유저의 입찰기록이 없습니다."));

        //bidStatus -> failed 변경, depositStatus -> Used 변경!
        bid.setStatus(BidStatus.CANCELED);
        bid.setDepositStatus(DepositStatus.USED);
        auction.setStatus(AuctionStatus.CLOSED);
        auction.setPaymentStatus(PaymentStatus.COMPLETED);
        auction.getSeller().addPoint(bid.getDepositAmount());
        pointService.saveCancelBidHistory(auction.getSeller(),bid.getDepositAmount());

        return new RefuseToPurchaseResponse(true);
    }
}