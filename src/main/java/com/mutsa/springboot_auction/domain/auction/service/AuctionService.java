package com.mutsa.springboot_auction.domain.auction.service;

import com.mutsa.springboot_auction.domain.auction.dto.AuctionDetailResponse;
import com.mutsa.springboot_auction.domain.auction.dto.AuctionListResponse;
import com.mutsa.springboot_auction.domain.auction.dto.AuctionRequest;
import com.mutsa.springboot_auction.domain.auction.dto.AuctionSimpleResponse;
import com.mutsa.springboot_auction.domain.auction.entity.Auction;
import com.mutsa.springboot_auction.domain.auction.repository.AuctionRepository;
import com.mutsa.springboot_auction.domain.auctionCategory.AuctionCategoryId;
import com.mutsa.springboot_auction.domain.auctionCategory.entity.AuctionCategory;
import com.mutsa.springboot_auction.domain.auctionCategory.repository.AuctionCategoryRepository;
import com.mutsa.springboot_auction.domain.auctionImage.entity.AuctionImage;
import com.mutsa.springboot_auction.domain.auctionImage.repository.AuctionImageRepository;
import com.mutsa.springboot_auction.domain.category.entity.Category;
import com.mutsa.springboot_auction.domain.category.repository.CategoryRepository;
import com.mutsa.springboot_auction.domain.user.entity.User;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final AuctionImageRepository auctionImageRepository;
    private final AuctionCategoryRepository auctionCategoryRepository;
    private final CategoryRepository categoryRepository;

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


    public AuctionDetailResponse get(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 Id의 경매가 존재하지 않습니다"));
        return AuctionDetailResponse.from(auction);
    }

    public AuctionListResponse getMyAuctions(User viewer) {
        return AuctionListResponse.of(auctionRepository.findBySeller(viewer).stream()
                .map(AuctionSimpleResponse::from).toList());
    }
}